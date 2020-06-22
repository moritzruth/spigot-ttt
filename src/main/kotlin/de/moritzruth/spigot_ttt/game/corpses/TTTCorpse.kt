package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.applyTypedMeta
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.time.Instant

class TTTCorpse private constructor(
    val tttPlayer: TTTPlayer,
    location: Location,
    private val role: Role,
    private val reason: DeathReason,
    private var credits: Int,
    velocity: Vector = Vector(),
    val spawnedBy: TTTPlayer? = null
) {
    private var status = Status.UNIDENTIFIED
    val entity: Zombie

    val inventory = tttPlayer.player.server.createInventory(null, InventoryType.HOPPER, "${role.chatColor}${tttPlayer.player.displayName}")
    val timestamp: Instant = Instant.now()

    val location get() = entity.location

    private var fullMinutesSinceDeath = 0
    private var updateTimeTask: BukkitTask

    init {
        inventory.setItem(ROLE_SLOT, ItemStack(role.iconItemMaterial).applyMeta {
            setDisplayName(role.coloredDisplayName)
            lore = listOf("${ChatColor.GRAY}Rolle")
        })

        setItems()

        entity = GameManager.world.spawnEntity(location.clone().also { it.pitch = 0F }, EntityType.ZOMBIE) as Zombie
        entity.apply {
            // Does not work for some reason :(
            // Therefore we have to use setAI(false), which ignores velocity
//            isAware = false
            setAI(false)
            this.velocity = velocity
            isSilent = true
            removeWhenFarAway = false
            isBaby = false
            isCollidable = false
        }

        entity.equipment!!.helmet = ItemStack(Material.PLAYER_HEAD).applyTypedMeta<SkullMeta> {
            // Question mark head
            @Suppress("DEPRECATION")
            owner = "MHF_question"
        }

        updateTimeTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            fullMinutesSinceDeath += 1
            setTimeItem()
        }, secondsToTicks(60).toLong(), secondsToTicks(60).toLong())
    }

    private fun setTimeItem() {
        if (status == Status.INSPECTED) {
            inventory.setItem(TIME_SLOT, ItemStack(Resourcepack.Items.time).applyMeta {
                val timeString =
                    if (fullMinutesSinceDeath == 0) "Vor weniger als einer Minute"
                    else "Vor weniger als ${fullMinutesSinceDeath + 1} Minuten"

                setDisplayName("${ChatColor.RESET}$timeString")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        } else {
            inventory.setItem(TIME_SLOT, ItemStack(Resourcepack.Items.time).applyMeta {
                setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        }
    }

    private fun setReasonItem() {
        if (status == Status.INSPECTED) {
            val reasonItemStack =
                if (reason is DeathReason.Item) reason.item.templateItemStack.clone()
                else ItemStack(Resourcepack.Items.deathReason)

            inventory.setItem(REASON_SLOT, reasonItemStack.applyMeta {
                setDisplayName("${ChatColor.RESET}" + reason.displayText)
                lore = listOf("${ChatColor.GRAY}Grund des Todes")
            })
        } else {
            inventory.setItem(REASON_SLOT, ItemStack(Resourcepack.Items.censored).applyMeta {
                setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
                lore = listOf("${ChatColor.GRAY}Grund des Todes")
            })
        }
    }

    private fun setItems() {
        setTimeItem()
        setReasonItem()
    }

    fun identify(by: TTTPlayer, inspect: Boolean) {
        ensureNotDestroyed()
        if (status == Status.UNIDENTIFIED) {
            GameMessenger.corpseIdentified(by.player.displayName, tttPlayer.player.displayName, role)

            if (!inspect) {
                status = Status.IDENTIFIED
            }
        }

        if (inspect && status != Status.INSPECTED) {
            status = Status.INSPECTED
            setItems()
        }

        entity.equipment!!.helmet = ItemStack(Material.PLAYER_HEAD).applyTypedMeta<SkullMeta> {
            owningPlayer = tttPlayer.player
        }

        if (credits != 0 && by.role.canOwnCredits) {
            val c = credits
            credits = 0
            by.credits += credits

            by.player.sendActionBarMessage(
                if (c > 1) "${ChatColor.GREEN}Du hast $c Credits aufgesammelt"
                else "${ChatColor.GREEN}Du hast 1 Credit aufgesammelt"
            )
        }
    }

    fun revive() {
        ensureNotDestroyed()
        tttPlayer.revive(entity.location, credits)
        destroy()
    }

    fun destroy() {
        ensureNotDestroyed()
        status = Status.DESTROYED
        entity.remove()
        updateTimeTask.cancel()
        inventory.viewers.toSet().forEach { it.closeInventory() }
        CorpseManager.corpses.remove(this)
    }

    private fun ensureNotDestroyed() {
        if (status == Status.DESTROYED) throw IllegalStateException("This corpse was destroyed")
    }

    enum class Status {
        DESTROYED,
        UNIDENTIFIED,
        IDENTIFIED,
        INSPECTED
    }

    companion object {
        private const val ROLE_SLOT = 0
        private const val REASON_SLOT = 1
        private const val TIME_SLOT = 2

        fun spawn(tttPlayer: TTTPlayer, reason: DeathReason): TTTCorpse = TTTCorpse(
            tttPlayer,
            tttPlayer.player.location,
            tttPlayer.role,
            reason,
            tttPlayer.credits,
            tttPlayer.player.velocity
        ).also { CorpseManager.add(it) }

        fun spawnFake(role: Role, tttPlayer: TTTPlayer, spawnedBy: TTTPlayer, location: Location) = TTTCorpse(
            tttPlayer,
            location,
            role,
            DeathReason.SUICIDE,
            0,
            spawnedBy = spawnedBy
        ).also { CorpseManager.add(it) }
    }
}
