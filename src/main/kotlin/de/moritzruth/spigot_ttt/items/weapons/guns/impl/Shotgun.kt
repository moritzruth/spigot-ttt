package de.moritzruth.spigot_ttt.items.weapons.guns.impl

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.Spawning
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

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.level = 0
        val state = (isc.get(tttPlayer) ?: return) as State

        val currentAction = state.currentAction

        if (currentAction is ReloadingAction) {
            state.currentAction = null
            currentAction.task.cancel()
            currentAction.updateTask.cancel()

            val meta = currentAction.itemStack.itemMeta as Damageable
            meta.damage = 0
            currentAction.itemStack.itemMeta = meta as ItemMeta
        }
    }

    override fun reload(tttPlayer: TTTPlayer, itemStack: ItemStack, state: Gun.State) {
        val ownState = state as State
        if (ownState.remainingShots == magazineSize) return

        ownState.currentAction = ReloadingAction(itemStack, ownState, tttPlayer).also { it.start() }
    }

    override fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: Gun.State): Boolean {
        val ownState = state as State
        if (ownState.remainingShots == 0) return true

        when(val currentAction = ownState.currentAction) {
            is Action.Cooldown -> throw ActionInProgressError()
            is ReloadingAction -> {
                ownState.currentAction = null
                currentAction.updateTask.cancel()
                currentAction.task.cancel()

                val damageMeta = item.itemMeta!! as Damageable
                damageMeta.damage = 0
                item.itemMeta = damageMeta as ItemMeta
            }
        }

        return true
    }

    class State: Gun.State(magazineSize)

    private class ReloadingAction(itemStack: ItemStack, state: State, tttPlayer: TTTPlayer): Action.Reloading(Shotgun, itemStack, state, tttPlayer) {
        lateinit var updateTask: BukkitTask

        override fun start() {
            task = startItemDamageProgress(
                itemStack,
                reloadTime,
                state.remainingShots.toDouble() / magazineSize
            ) { state.currentAction = null }

            updateTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                state.remainingShots++
                updateLevel(tttPlayer)

                // TODO: Add sound

                if (state.remainingShots == magazineSize) {
                    this.updateTask.cancel()
                }
            },
                secondsToTicks(RELOAD_TIME_PER_BULLET).toLong(),
                secondsToTicks(RELOAD_TIME_PER_BULLET).toLong())
        }
    }
}

