package de.moritzruth.spigot_ttt.game.worlds

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.createTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AddSpawnLocationCommand: CommandExecutor {
    init {
        plugin.getCommand("addspawnlocation")?.let {
            it.setExecutor(this)
            it.tabCompleter = createTabCompleter { _, index -> when(index) {
                0 -> listOf("item", "player")
                else -> null
            } }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Dieser Befehl kann nur von Spielern verwendet werden.")
        } else if (args.count() == 0) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Falsche Verwendung.")
        } else {
            val tttWorld = GameManager.tttWorld
            if (tttWorld == null) {
                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Du befindest dich nicht in einer TTT-Welt.")
            } else {
                when(args[0].toLowerCase()) {
                    "item" -> tttWorld.spawnLocations.addItemSpawnLocation(sender.location)
                    "player" -> tttWorld.spawnLocations.addPlayerSpawnLocation(sender.location)
                }

                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.GREEN}Ein Spawnpunkt wurde an deiner " +
                        "Position hinzugefügt.")
            }
        }

        return true
    }
}
