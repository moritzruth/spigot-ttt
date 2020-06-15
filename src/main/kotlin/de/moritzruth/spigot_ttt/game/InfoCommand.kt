package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.createTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class InfoCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("info")!!
        command.tabCompleter = createTabCompleter { _, index ->
            if (index == 1) PlayerManager.tttPlayers.map { it.player.name }
            else null
        }
        command.setExecutor(this)
    }

    private fun getStatus(tttPlayer: TTTPlayer) =
        if (tttPlayer.alive) "${ChatColor.GREEN}Lebend" else "${ChatColor.RED}Tot"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase === null) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Zurzeit läuft kein Spiel.")
            return true
        }

        val lines = mutableListOf<String>()

        if (args.count() == 1) {
            val playerName = args[0]
            val player = plugin.server.getPlayer(playerName)

            if (player == null) {
                lines.add("$playerName ${ChatColor.RED}existiert nicht oder ist nicht online.")
            } else {
                val tttPlayer = TTTPlayer.of(player)

                if (tttPlayer == null) {
                    lines.add("$COMMAND_RESPONSE_PREFIX${ChatColor.WHITE}${player.name} ${ChatColor.RED}spielt nicht mit.")
                } else {
                    lines.add("===== ${ChatColor.BOLD}Spielerinfo: ${player.name}${ChatColor.RESET} =====")
                    lines.add(" ")
                    lines.add("Rolle: ${tttPlayer.role.coloredDisplayName}")
                    lines.add("Klasse: ${tttPlayer.tttClass.coloredDisplayName}")
                    lines.add("Status: ${getStatus(tttPlayer)}")
                    lines.add(" ")
                }
            }
        } else {
            lines.add("=========== ${ChatColor.BOLD}Spielinfo${ChatColor.RESET} ===========")
            lines.add(" ")
            lines.add("${ChatColor.GRAY}Rolle - Klasse - Status")

            for (tttPlayer in PlayerManager.tttPlayers) {
                val values = listOf(
                    tttPlayer.role.coloredDisplayName,
                    tttPlayer.tttClass.coloredDisplayName,
                    getStatus(tttPlayer)
                )

                lines.add("${tttPlayer.player.name}: ${values.joinToString("${ChatColor.RESET} - ")}")
            }

            lines.add(" ")
        }

        sender.sendMessage(lines.joinToString("\n${ChatColor.RESET}"))
        return true
    }
}
