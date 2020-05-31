package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.SelectableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

abstract class Gun<StateT: GunState>: TTTItem, SelectableItem {

    @Suppress("LeakingThis")
    override val listener: Listener = GunListener(this)

    abstract val damage: Double
    abstract val cooldown: Double
    abstract val reloadTime: Double
    abstract val magazineSize: Int
    abstract val itemMaterial: Material
    abstract val recoil: Int

    abstract fun getState(tttPlayer: TTTPlayer): StateT

    open fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player) = damage

    protected fun getItemMeta(itemStack: ItemStack): ItemMeta {
        val meta = itemStack.itemMeta!!
        meta.setDisplayName(displayName)
        meta.lore = listOf(
                "",
                "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(damage)}",
                "${ChatColor.GRAY}Cooldown: ${LoreHelper.cooldown(cooldown)}",
                "${ChatColor.GRAY}Magazin: ${LoreHelper.uses(magazineSize)} Schuss"
        )

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        return meta
    }

    protected fun updateLevel(tttPlayer: TTTPlayer, state: StateT = getState(tttPlayer)) {
        tttPlayer.player.level = state.remainingShots
    }

    private fun applyRecoil(player: Player) {
        val location = player.location
        location.pitch -= recoil
        player.teleport(location)
    }

    open fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: StateT = getState(tttPlayer)) {
        if (state.cooldownOrReloadTask !== null) throw ActionInProgressError()
    }

    fun shoot(tttPlayer: TTTPlayer, item: ItemStack, state: StateT = getState(tttPlayer)) {
        onBeforeShoot(tttPlayer, item, state)

        if (state.remainingShots == 0) {
            GameManager.world.playSound(tttPlayer.player.location, Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1f, 1.3f)
            return
        }

        // TODO: Add sound
        GameManager.world.playSound(tttPlayer.player.location, Sound.BLOCK_IRON_DOOR_OPEN, SoundCategory.PLAYERS, 2f, 1.3f)

        state.remainingShots--
        updateLevel(tttPlayer)

        if (GameManager.phase == GamePhase.COMBAT) {
            val rayTraceResult = GameManager.world.rayTrace(
                    tttPlayer.player.eyeLocation,
                    tttPlayer.player.eyeLocation.direction,
                    200.0,
                    FluidCollisionMode.ALWAYS,
                    true,
                    0.01
            ) { it !== tttPlayer.player }

            if (rayTraceResult !== null) {
                val entity = rayTraceResult.hitEntity

                if (entity is Player) {
                    val actualDamage = computeActualDamage(tttPlayer, entity)

                    entity.damage(actualDamage)
                    tttPlayer.player.playSound(tttPlayer.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 2f, 1.2f)
                    entity.velocity = tttPlayer.player.location.direction.multiply(actualDamage / 20)
                }
            }
        }

        applyRecoil(tttPlayer.player)

        state.cooldownOrReloadTask = startItemDamageProgress(item, cooldown) {
            state.cooldownOrReloadTask = null
        }
    }

    open fun reload(tttPlayer: TTTPlayer, item: ItemStack, state: StateT = getState(tttPlayer)) {
        if (state.cooldownOrReloadTask !== null) throw ActionInProgressError()
        if (state.remainingShots == magazineSize) return

        state.cooldownOrReloadTask = startItemDamageProgress(item, reloadTime) {
            state.cooldownOrReloadTask = null
            state.remainingShots = magazineSize
            updateLevel(tttPlayer, state)
        }

        // TODO: Add sound
    }

    override fun onSelect(tttPlayer: TTTPlayer) {
        updateLevel(tttPlayer)
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.level = 0
    }

    class ActionInProgressError: RuntimeException("The gun is on cooldown or reloading")
}
