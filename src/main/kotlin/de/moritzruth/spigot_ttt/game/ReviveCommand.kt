package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.createTabCompleter
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReviveCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("revive")!!

        command.setExecutor(this)
        command.tabCompleter = createTabCompleter { sender, index ->
            fun getPlayers() = PlayerManager.tttPlayers
                .filter { !it.alive }
                .map { it.player.name }

            if (sender is Player) when (index) {
                0 -> getPlayers().filter { it != sender.name }.run {
                    if (TTTPlayer.of(sender)?.alive == false) plus("here")
                    else this
                }
                1 -> listOf("here")
                else -> null
            } else when (index) {
                0 -> getPlayers()
                else -> null
            }
        }
    }

    private val invalidUseMessage = "$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Falsche Verwendung. Syntax: " +
            "${ChatColor.WHITE}/revive [Player] ['here']"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase == null) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Zurzeit läuft kein Spiel.")
        } else {
            fun continueWithPlayer(
                player: Player,
                isSelf: Boolean,
                location: Location = GameManager.world.spawnLocation
            ) {
                val tttPlayer = TTTPlayer.of(player)

                if (tttPlayer == null) {
                    if (isSelf) {
                        sender.sendMessage(
                            "$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Du kannst dich nicht wiederbeleben, " +
                                    "da du nicht mitspielst."
                        )
                    } else {
                        sender.sendMessage(
                            "$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Du kannst ${ChatColor.WHITE}${player.name} ${ChatColor.RED}nicht wiederbeleben, " +
                                    "da er/sie nicht mitspielst."
                        )
                    }
                } else {
                    try {
                        tttPlayer.revive(location)
                        if (!isSelf) sender.sendMessage("$COMMAND_RESPONSE_PREFIX${player.name} ${ChatColor.GREEN}wurde wiederbelebt.")
                    } catch (e: TTTPlayer.AlreadyLivingException) {
                        if (isSelf) sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Du lebst bereits.")
                        else sender.sendMessage("$COMMAND_RESPONSE_PREFIX${player.name} ${ChatColor.RED}lebt bereits.")
                    }
                }
            }

            fun continueWithPlayerName(
                playerName: String,
                location: Location = GameManager.world.spawnLocation
            ) {
                val player = plugin.server.getPlayer(playerName)

                if (player == null) {
                    sender.sendMessage(
                        "$COMMAND_RESPONSE_PREFIX$playerName ${ChatColor.RED}existiert nicht oder ist nicht online."
                    )
                } else continueWithPlayer(player, player == sender, location)
            }

            if (sender is Player) {
                when(args.count()) {
                    0 -> continueWithPlayer(sender, isSelf = true)
                    1 ->
                        if (args[0].equals("here", true))
                            continueWithPlayer(sender, isSelf = true, location = sender.location)
                        else continueWithPlayerName(args[0])
                    2 ->
                        if (args[1].equals("here", true))
                            continueWithPlayerName(args[0], sender.location)
                        else sender.sendMessage(invalidUseMessage)
                    else -> sender.sendMessage(invalidUseMessage)
                }
            } else {
                when(args.count()) {
                    0 -> sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Bitte gib einen Spieler an.")
                    1 -> continueWithPlayerName(args[0])
                    else -> sender.sendMessage(invalidUseMessage)
                }
            }
        }

        return true
    }
}
