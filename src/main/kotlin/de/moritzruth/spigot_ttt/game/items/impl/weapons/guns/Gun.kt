package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.LoreHelper
import de.moritzruth.spigot_ttt.game.items.SpawnProbability
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.startExpProgressTask
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Instant
import kotlin.reflect.KClass

abstract class Gun(
    type: Type,
    instanceType: KClass<out Instance>,
    spawnProbability: SpawnProbability? = null,
    shopInfo: ShopInfo? = null,
    material: Material,
    displayName: String,
    itemLore: List<String>? = null,
    appendLore: Boolean = true,
    val damage: Double,
    val cooldown: Double,
    val magazineSize: Int,
    val reloadTime: Double,
    val shootSound: String,
    val reloadSound: String
): TTTItem<Gun.Instance>(
    type = type,
    instanceType = instanceType,
    spawnProbability = spawnProbability,
    shopInfo = shopInfo,
    templateItemStack = ItemStack(material).applyMeta {
        setDisplayName(displayName)
        lore =
            if (appendLore) listOf(
                "",
                "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(if (damage < 0) null else (damage / 2))}",
                "${ChatColor.GRAY}Cooldown: ${LoreHelper.cooldown(cooldown)}",
                "${ChatColor.GRAY}Magazin: ${LoreHelper.uses(magazineSize)} Schuss"
            ) + run {
                if (itemLore == null) emptyList()
                else listOf("") + itemLore
            }
            else itemLore ?: emptyList()

        hideInfo()
    }
) {
    open class Instance(val gun: Gun): TTTItem.Instance(gun) {
        var currentAction: Action? = null
        var remainingShots: Int = gun.magazineSize
            set(value) {
                field = value
                setCarrierLevel()
            }

        private fun setCarrierLevel() {
            if (isSelected) carrier!!.player.level = remainingShots
        }

        private fun shoot() {
            val tttPlayer = requireCarrier()
            if (!onBeforeShoot()) return

            if (remainingShots == 0) {
                GameManager.world.playSound(
                    tttPlayer.player.location,
                    Resourcepack.Sounds.Item.Weapon.Generic.emptyMagazine,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                return
            }

            GameManager.world.playSound(tttPlayer.player.location, gun.shootSound, SoundCategory.PLAYERS, 1F, 1F)

            remainingShots--

            val rayTraceResult = GameManager.world.rayTrace(
                tttPlayer.player.eyeLocation,
                tttPlayer.player.eyeLocation.direction,
                200.0,
                FluidCollisionMode.ALWAYS,
                true,
                0.01
            ) { it !== tttPlayer.player }

            if (rayTraceResult !== null) {
                val hitBlock = rayTraceResult.hitBlock
                if (hitBlock != null) GameManager.destroyBlockIfAllowed(hitBlock)

                val entity = rayTraceResult.hitEntity

                if (entity is Player) {
                    val damagedTTTPlayer = TTTPlayer.of(entity)

                    if (damagedTTTPlayer != null) {
                        onHit(tttPlayer, damagedTTTPlayer)
                    }
                }
            }

            currentAction = Action.Cooldown(this)
        }

        open fun reload() {
            val carrier = requireCarrier()
            if (currentAction != null) throw ActionInProgressError()
            if (remainingShots == gun.magazineSize) return

            currentAction = Action.Reloading(this)

            GameManager.world.playSound(
                carrier.player.location,
                gun.reloadSound,
                SoundCategory.PLAYERS,
                1F,
                1F
            )
        }

        open fun computeActualDamage(receiver: TTTPlayer): Double {
            requireCarrier() // Only to keep parity with possible override
            return if (gun.damage < 0 ) 1000.0 else gun.damage
        }

        /**
         * @return Whether the gun will really shoot
         */
        open fun onBeforeShoot(): Boolean {
            if (currentAction !== null) throw ActionInProgressError()
            return true
        }

        open fun onHit(tttPlayer: TTTPlayer, hitTTTPlayer: TTTPlayer) {
            val actualDamage = computeActualDamage(hitTTTPlayer)
            hitTTTPlayer.damage(actualDamage, DeathReason.Item(gun), tttPlayer, true)
            tttPlayer.player.playSound(
                tttPlayer.player.location,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER,
                2f,
                1.2f
            )
            hitTTTPlayer.player.velocity = tttPlayer.player.location.direction.multiply(
                (actualDamage / 20).coerceAtMost(3.0)
            )
        }

        override fun onLeftClick(event: ClickEvent) {
            try {
                reload()
            } catch (e: ActionInProgressError) {}
        }

        override fun onRightClick(event: ClickEvent) {
            try {
                shoot()
            } catch (e: ActionInProgressError) {}
        }

        override fun onCarrierSet(carrier: TTTPlayer, isFirst: Boolean) {
            setCarrierLevel()
        }

        override fun onSelect() {
            setCarrierLevel()
        }

        override fun onDeselect() {
            val carrier = carrier!!
            carrier.player.level = 0
            carrier.player.exp = 0F

            val action = currentAction
            if (action is Action.Reloading) {
                currentAction = null
                action.cancel()
            }
        }
    }

    class ActionInProgressError: RuntimeException("The gun has an ongoing action")

    sealed class Action(val instance: Instance) {
        val startedAt = Instant.now()!!
        abstract val task: BukkitTask

        open fun cancel() {
            task.cancel()
        }

        open class Reloading(instance: Instance): Action(instance) {
            override val task = createProgressTask()

            protected open fun createProgressTask() =
                instance.startExpProgressTask(instance.gun.reloadTime) {
                    instance.remainingShots = instance.gun.magazineSize
                    instance.currentAction = null
                }
        }

        class Cooldown(instance: Instance): Action(instance) {
            override val task = instance.startExpProgressTask(instance.gun.cooldown) {
                instance.currentAction = null
            }
        }
    }
}
