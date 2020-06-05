package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scheduler.BukkitTask

private const val RELOAD_TIME_PER_BULLET = 0.5
private const val MAGAZINE_SIZE = 8

object Shotgun: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Shotgun",
    damage = heartsToHealth(3.0),
    cooldown = 0.8,
    magazineSize = MAGAZINE_SIZE,
    reloadTime = RELOAD_TIME_PER_BULLET * MAGAZINE_SIZE,
    itemMaterial = CustomItems.shotgun,
    additionalLore = listOf("${ChatColor.RED}Weniger Schaden auf Distanz")
), Spawning {
    override val type = TTTItem.Type.HEAVY_WEAPON

    override fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player): Double {
        val distance = tttPlayer.player.location.distance(receiver.location)

        return when {
            distance <= 2 -> heartsToHealth(10.0)
            distance >= 14 -> 0.0
            distance > 8 -> heartsToHealth(1.5)
            else -> heartsToHealth(damage)
        }
    }

    override fun reload(tttPlayer: TTTPlayer, item: ItemStack, state: Gun.State) {
        val ownState = state as State
        if (ownState.remainingShots == magazineSize) return

        ownState.cooldownOrReloadTask = startItemDamageProgress(item,
            reloadTime, ownState.remainingShots.toDouble() / magazineSize
        ) {
            ownState.cooldownOrReloadTask = null
        }

        ownState.reloadUpdateTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            ownState.remainingShots++
            updateLevel(tttPlayer)

            // TODO: Add sound

            if (ownState.remainingShots == magazineSize) {
                ownState.reloadUpdateTask?.cancel()
                ownState.reloadUpdateTask = null
            }
        }, secondsToTicks(RELOAD_TIME_PER_BULLET).toLong(), secondsToTicks(
            RELOAD_TIME_PER_BULLET
        ).toLong())
    }

    override fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: Gun.State) {
        val ownState = state as State
        if (ownState.remainingShots == 0) return

        if (ownState.reloadUpdateTask == null && ownState.cooldownOrReloadTask != null) {
            throw ActionInProgressError()
        }

        ownState.cooldownOrReloadTask?.cancel()
        ownState.cooldownOrReloadTask = null

        val damageMeta = item.itemMeta!! as Damageable
        damageMeta.damage = 0
        item.itemMeta = damageMeta as ItemMeta

        ownState.reloadUpdateTask?.cancel()
        ownState.reloadUpdateTask = null
    }

    class State: Gun.State(magazineSize) {
        var reloadUpdateTask: BukkitTask? = null
    }
}


