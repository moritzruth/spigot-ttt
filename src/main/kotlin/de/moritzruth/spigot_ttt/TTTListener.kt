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
        event.joinMessage = "${TTTPlugin.prefix}${event.player.displayName} ${ChatColor.GOLD}hat das Spiel betreten."
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
