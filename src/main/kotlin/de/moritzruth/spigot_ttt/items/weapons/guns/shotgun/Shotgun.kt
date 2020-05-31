package de.moritzruth.spigot_ttt.items.weapons.guns.shotgun

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

object Shotgun: Gun<ShotgunState>() {
    private const val reloadTimePerBullet = 0.5

    override val spawning = true
    override val displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Shotgun"
    override val damage = heartsToHealth(3.0)
    override val cooldown = 0.8
    override val magazineSize = 8
    override val reloadTime get() = reloadTimePerBullet * magazineSize
    override val recoil = 20
    override val type = TTTItem.Type.NORMAL_WEAPON

    override val itemMaterial = CustomItems.shotgun
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this).apply {
            lore = lore!! + listOf("", "${ChatColor.RED}Weniger Schaden auf Distanz")
        }
    }

    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(ShotgunState::class) { ShotgunState() }

    override fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player): Double {
        val distance = tttPlayer.player.location.distance(receiver.location)

        return when {
            distance <= 2 -> heartsToHealth(10.0)
            distance >= 14 -> 0.0
            distance > 8 -> heartsToHealth(1.5)
            else -> heartsToHealth(damage)
        }
    }

    override fun reload(tttPlayer: TTTPlayer, item: ItemStack, state: ShotgunState) {
        if (state.remainingShots == magazineSize) return

        state.cooldownOrReloadTask = startItemDamageProgress(item, reloadTime, state.remainingShots.toDouble() / magazineSize) {
            state.cooldownOrReloadTask = null
        }

        state.reloadUpdateTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            state.remainingShots++
            updateLevel(tttPlayer)

            // TODO: Add sound

            if (state.remainingShots == magazineSize) {
                state.reloadUpdateTask?.cancel()
                state.reloadUpdateTask = null
            }
        }, secondsToTicks(reloadTimePerBullet).toLong(), secondsToTicks(reloadTimePerBullet).toLong())
    }

    override fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: ShotgunState) {
        if (state.remainingShots == 0) return

        if (state.reloadUpdateTask == null && state.cooldownOrReloadTask != null) {
            throw ActionInProgressError()
        }

        state.cooldownOrReloadTask?.cancel()
        state.cooldownOrReloadTask = null
        val damageMeta = item.itemMeta!! as Damageable
        damageMeta.damage = 0
        item.itemMeta = damageMeta as ItemMeta

        state.reloadUpdateTask?.cancel()
        state.reloadUpdateTask = null
    }
}


