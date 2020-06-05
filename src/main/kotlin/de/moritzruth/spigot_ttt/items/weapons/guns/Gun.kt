package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.players.IState
import de.moritzruth.spigot_ttt.game.players.InversedStateContainer
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.noop
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import kotlin.reflect.KClass

abstract class Gun(
    stateClass: KClass<out State>,
    displayName: String,
    additionalLore: List<String>? = null,
    val damage: Double,
    val cooldown: Double,
    val magazineSize: Int,
    val reloadTime: Double,
    itemMaterial: Material
): TTTItem, Selectable {
    override val itemStack = ItemStack(itemMaterial).applyMeta {
        setDisplayName(displayName)
        lore = listOf(
            "",
            "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(if (damage < 0) null else (damage / 2))}",
            "${ChatColor.GRAY}Cooldown: ${LoreHelper.cooldown(cooldown)}",
            "${ChatColor.GRAY}Magazin: ${LoreHelper.uses(magazineSize)} Schuss"
        ) + run {
            if (additionalLore == null) emptyList()
            else listOf("") + additionalLore
        }

        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }

    val isc = InversedStateContainer(stateClass)

    protected fun updateLevel(tttPlayer: TTTPlayer, state: State = isc.get(tttPlayer)) {
        tttPlayer.player.level = state.remainingShots
    }

    fun shoot(tttPlayer: TTTPlayer, item: ItemStack, state: State = isc.get(tttPlayer)) {
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

                    PlayerManager.getTTTPlayer(entity)?.itemOfLastDamage = this

                    entity.damage(actualDamage)
                    tttPlayer.player.playSound(tttPlayer.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 2f, 1.2f)
                    entity.velocity = tttPlayer.player.location.direction.multiply(actualDamage / 20)
                }
            }
        }

        state.cooldownOrReloadTask = startItemDamageProgress(item, cooldown) {
            state.cooldownOrReloadTask = null
        }
    }

    open fun reload(tttPlayer: TTTPlayer, item: ItemStack, state: State = isc.get(tttPlayer)) {
        if (state.cooldownOrReloadTask !== null) throw ActionInProgressError()
        if (state.remainingShots == magazineSize) return

        state.cooldownOrReloadTask = startItemDamageProgress(item, reloadTime) {
            state.cooldownOrReloadTask = null
            state.remainingShots = magazineSize
            updateLevel(tttPlayer, state)
        }

        // TODO: Add sound
    }

    open fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player) = if (damage < 0 ) 1000.0 else damage

    open fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: State = isc.get(tttPlayer)) {
        if (state.cooldownOrReloadTask !== null) throw ActionInProgressError()
    }

    override fun reset(tttPlayer: TTTPlayer) {
        isc.get(tttPlayer).cooldownOrReloadTask?.cancel()
    }

    override fun onSelect(tttPlayer: TTTPlayer) {
        updateLevel(tttPlayer)
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.level = 0
    }

    override val listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(this@Gun)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(this@Gun)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            try {
                when(event.action) {
                    Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> reload(tttPlayer, event.item!!)
                    Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> shoot(tttPlayer, event.item!!)
                    else -> noop()
                }
            } catch (e: ActionInProgressError) {}
        }
    }

    class ActionInProgressError: RuntimeException("The gun is on cooldown or reloading")

    open class State(magazineSize: Int): IState {
        var cooldownOrReloadTask: BukkitTask? = null
        var remainingShots = magazineSize
    }

    companion object {
        const val INFINITE_DAMAGE: Double = -1.0
    }
}
