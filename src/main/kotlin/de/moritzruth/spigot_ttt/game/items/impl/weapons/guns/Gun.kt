package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.game.items.*
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.nextTick
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

typealias ClickAction = org.bukkit.event.block.Action

abstract class Gun(
    private val stateClass: KClass<out State>,
    displayName: String,
    additionalLore: List<String>? = null,
    val damage: Double,
    val cooldown: Double,
    val magazineSize: Int,
    val reloadTime: Double,
    val itemMaterial: Material,
    val shootSound: String,
    val reloadSound: String
): TTTItem, Selectable, DropHandler {
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

    protected fun updateLevel(tttPlayer: TTTPlayer, state: State = isc.getOrCreate(tttPlayer)) {
        tttPlayer.player.level = state.remainingShots
    }

    fun shoot(tttPlayer: TTTPlayer, itemStack: ItemStack, state: State = isc.getOrCreate(tttPlayer)) {
        if (!onBeforeShoot(tttPlayer, itemStack, state)) return

        if (state.remainingShots == 0) {
            GameManager.world.playSound(
                tttPlayer.player.location,
                Resourcepack.Sounds.Item.Weapon.Generic.emptyMagazine,
                SoundCategory.PLAYERS,
                1F,
                1F
            )
            return
        }

        GameManager.world.playSound(tttPlayer.player.location, shootSound, SoundCategory.PLAYERS, 1F, 1F)

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
                    val damagedTTTPlayer = TTTPlayer.of(entity)

                    if (damagedTTTPlayer != null) {
                        onHit(tttPlayer, damagedTTTPlayer)
                    }
                }
            }
        }

        state.currentAction = Action.Cooldown(this, itemStack, state)
    }

    open fun reload(tttPlayer: TTTPlayer, itemStack: ItemStack, state: State = isc.getOrCreate(tttPlayer)) {
        if (state.currentAction != null) throw ActionInProgressError()
        if (state.remainingShots == magazineSize) return

        state.currentAction = Action.Reloading(this, itemStack, state, tttPlayer).also { it.start() }

        GameManager.world.playSound(tttPlayer.player.location, reloadSound, SoundCategory.PLAYERS, 1F, 1F)
    }

    open fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player) = if (damage < 0 ) 1000.0 else damage

    open fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: State = isc.getOrCreate(tttPlayer)): Boolean {
        if (state.currentAction !== null) throw ActionInProgressError()
        return true
    }

    open fun onHit(tttPlayer: TTTPlayer, hitTTTPlayer: TTTPlayer) {
        hitTTTPlayer.damageInfo = DamageInfo(tttPlayer, DeathReason.Item(this))
        val actualDamage = computeActualDamage(tttPlayer, hitTTTPlayer.player)

        hitTTTPlayer.player.damage(actualDamage)
        tttPlayer.player.playSound(tttPlayer.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 2f, 1.2f)
        hitTTTPlayer.player.velocity = tttPlayer.player.location.direction.multiply(
            (actualDamage / 20).coerceAtMost(3.0)
        )
    }

    override fun onSelect(tttPlayer: TTTPlayer) {
        updateLevel(tttPlayer)
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.level = 0

        val state = isc.get(tttPlayer) ?: return
        val currentAction = state.currentAction

        if (currentAction is Action.Reloading) {
            state.currentAction = null
            currentAction.task.cancel()

            val meta = currentAction.itemStack.itemMeta as Damageable
            meta.damage = 0
            currentAction.itemStack.itemMeta = meta as ItemMeta
        }
    }

    override fun onDrop(tttPlayer: TTTPlayer, itemEntity: Item): Boolean {
        val state = isc.get(tttPlayer) ?: return true

        when(val currentAction = state.currentAction) {
            is Action.Reloading -> {
                state.currentAction = null
                currentAction.task.cancel()
            }
            is Action.Cooldown -> {
                currentAction.pause()
            }
        }

        itemEntity.setItemStack(itemStack.clone())

        ItemManager.droppedItemStates[itemEntity.entityId] = state
        isc.remove(tttPlayer)
        return true
    }

    override fun onPickup(tttPlayer: TTTPlayer, itemEntity: Item) {
        val state = ItemManager.droppedItemStates[itemEntity.entityId] as State?

        if (state != null) {
            tttPlayer.stateContainer.put(stateClass, state)
            val currentAction = state.currentAction ?: return

            nextTick { currentAction.itemStack = tttPlayer.player.inventory.find { it.type == itemEntity.itemStack.type }!!

                if (currentAction is Action.Cooldown) {
                    currentAction.resume()
                } }
        }
    }

    override val listener = object : TTTItemListener(this, true) {
        override fun onLeftClick(data: ClickEventData) {
            try {
                reload(data.tttPlayer, data.event.item!!)
            } catch (e: ActionInProgressError) {}
        }

        override fun onRightClick(data: ClickEventData) {
            try {
                shoot(data.tttPlayer, data.event.item!!)
            } catch (e: ActionInProgressError) {}
        }

        @EventHandler
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) = isc.get(event.tttPlayer)?.reset()

        @EventHandler
        fun onGameEnd(event: GameEndEvent) = isc.forEveryState { state, _ -> state.reset() }
    }

    class ActionInProgressError: RuntimeException("The gun has an ongoing action which may not be canceled")

    abstract class State(magazineSize: Int): IState {
        var currentAction: Action? = null
        var remainingShots = magazineSize

        fun reset() { currentAction?.reset() }
    }

    sealed class Action(var itemStack: ItemStack) {
        val startedAt = Instant.now()!!
        abstract var task: BukkitTask; protected set

        open fun reset() {
            task.cancel()
        }

        open class Reloading(
            private val gun: Gun,
            itemStack: ItemStack,
            protected val state: State,
            protected val tttPlayer: TTTPlayer
        ): Action(itemStack) {
            override lateinit var task: BukkitTask

            open fun start() {
                task = startItemDamageProgress(itemStack, gun.reloadTime) {
                    state.currentAction = null
                    state.remainingShots = gun.magazineSize
                    gun.updateLevel(tttPlayer, state)
                }
            }
        }

        class Cooldown(private val gun: Gun, itemStack: ItemStack, private val state: State): Action(itemStack) {
            override var task = startTask()
            private var pausedProgress: Double? = null

            private fun startTask() = startItemDamageProgress(
                itemStack = itemStack,
                duration = gun.cooldown,
                startProgress = pausedProgress ?: 0.0
            ) {
                state.currentAction = null
            }

            fun resume() {
                if (task.isCancelled) task = startTask()
            }

            fun pause() {
                if (!task.isCancelled) {
                    task.cancel()
                    pausedProgress = (Duration.between(startedAt, Instant.now()).toMillis().toDouble() / 1000) / gun.cooldown
                }
            }
        }
    }

    companion object {
        const val INFINITE_DAMAGE: Double = -1.0
    }
}
