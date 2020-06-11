package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.GameManager
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent

object TTTListener: Listener {
    fun register() {
        plugin.server.pluginManager.registerEvents(TTTListener, plugin)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.player.gameMode !== GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.player.gameMode !== GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.setPlayerListHeaderFooter(
            "\n${ChatColor.GREEN}${ChatColor.BOLD}Willkommen ${ChatColor.WHITE}${player.name}!\n ",
            "\n    ${ChatColor.RED}Sobald du stirbst, darfst du ${ChatColor.BOLD}nicht mehr reden.    \n\n" +
                    "${ChatColor.GRAY}${ChatColor.BOLD}TTT v1.0.0 by zziius\n "
        )

        event.joinMessage = "${TTTPlugin.prefix}${player.displayName} ${ChatColor.GOLD}hat das Spiel betreten."
        player.setResourcePack(Resourcepack.url, Resourcepack.checksum)
    }

    @EventHandler
    fun onPlayerResourcePackStatus(event: PlayerResourcePackStatusEvent) {
        when (event.status) {
            PlayerResourcePackStatusEvent.Status.DECLINED -> {
                event.player.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Du hast das Resourcepack abgelehnt.")
                event.player.sendMessage("${TTTPlugin.prefix}${ChatColor.GREEN}${ChatColor.BOLD}Wenn du es dir anders " +
                        "überlegst, entferne den Server aus deiner Serverlist und tritt ihm erneut bei.")
            }
            PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD -> {
                event.player.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Das Laden des Resourcepacks " +
                        "ist gescheitert.")
                event.player.sendMessage("${TTTPlugin.prefix}${ChatColor.GREEN}Mit " +
                        "${ChatColor.WHITE}/rp " +
                        "${ChatColor.GREEN}kannst du es erneut versuchen.")
            }
            PlayerResourcePackStatusEvent.Status.ACCEPTED -> {
                event.player.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.AQUA}Das Resourcepack wird geladen...")
            }
            PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED -> {
                event.player.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.GREEN}Das Resourcepack wurde erfolgreich geladen.")
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage = "${TTTPlugin.prefix}${event.player.displayName} ${ChatColor.GOLD}hat das Spiel verlassen."
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (GameManager.phase == null) event.isCancelled = true
    }
}
