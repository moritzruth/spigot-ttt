package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.Probability
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.startProgressTask
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.scheduler.BukkitTask

private const val RELOAD_TIME_PER_BULLET = 0.5
private const val MAGAZINE_SIZE = 8

object Shotgun: Gun(
    type = Type.HEAVY_WEAPON,
    instanceType = Instance::class,
    spawnProbability = Probability.LOW,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Shotgun",
    damage = heartsToHealth(3.0),
    cooldown = 0.9,
    magazineSize = MAGAZINE_SIZE,
    reloadTime = RELOAD_TIME_PER_BULLET * MAGAZINE_SIZE,
    material = Resourcepack.Items.shotgun,
    itemLore = listOf("${ChatColor.RED}Weniger Schaden auf Distanz"),
    shootSound = Resourcepack.Sounds.Item.Weapon.Shotgun.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Shotgun.reload
) {
    class Instance: Gun.Instance(Shotgun) {
        override fun computeActualDamage(receiver: TTTPlayer): Double {
            val distance = requireCarrier().player.location.distance(receiver.player.location)

            return when {
                distance <= 1 -> heartsToHealth(10.0)
                distance >= 14 -> 0.5
                distance > 8 -> heartsToHealth(1.5)
                else -> heartsToHealth(damage)
            }
        }

        override fun onDeselect() {
            carrier!!.player.level = 0
            carrier!!.player.exp = 0F

            val action = currentAction
            if (action is ReloadingAction) {
                currentAction = null
                action.cancel()
            }
        }

        override fun reload() {
            if (currentAction != null) throw ActionInProgressError()
            if (remainingShots == magazineSize) return
            currentAction = ReloadingAction(this)
        }

        override fun onBeforeShoot(): Boolean {
            if (remainingShots == 0) return true

            when(val action = currentAction) {
                is Action.Cooldown -> throw ActionInProgressError()
                is ReloadingAction -> action.cancel()
            }

            return true
        }
    }

    private class ReloadingAction(instance: Instance): Action.Reloading(instance) {
        override fun createProgressTask(): BukkitTask = startProgressTask(
            instance.gun.reloadTime,
            startAt = instance.remainingShots.toDouble() / instance.gun.magazineSize
        ) { data ->
            val exp = if (data.isComplete) {
                instance.currentAction = null
                0F
            } else data.progress.toFloat()
            if (instance.isSelected) instance.carrier!!.player.exp = exp
        }

        private var updateTask: BukkitTask? = null

        init {
            updateTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
                instance.remainingShots++
                GameManager.world.playSound(instance.carrier!!.player.location, reloadSound, SoundCategory.PLAYERS, 1F, 1F)
                if (instance.remainingShots == magazineSize) updateTask?.cancel()
            }, secondsToTicks(RELOAD_TIME_PER_BULLET).toLong(), secondsToTicks(RELOAD_TIME_PER_BULLET).toLong())
        }

        override fun cancel() {
            task.cancel()
            updateTask?.cancel()
        }
    }
}


