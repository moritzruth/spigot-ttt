package de.moritzruth.spigot_ttt.game.corpses

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI
import org.golde.bukkit.corpsereborn.nms.Corpses
import java.time.Instant

class TTTCorpse private constructor(
    private val player: Player,
    location: Location,
    private val role: Role,
    private val reason: DeathReason,
    private var credits: Int
) {
    var status = Status.UNIDENTIFIED; private set

    val corpse: Corpses.CorpseData?
    val inventory = player.server.createInventory(null, InventoryType.HOPPER, "${role.chatColor}${player.displayName}")

    val timestamp: Instant = Instant.now()
    private var fullMinutesSinceDeath = 0
    private var updateTimeListener: BukkitTask

    init {
        inventory.setItem(ROLE_SLOT, ItemStack(role.iconItemMaterial).applyMeta {
            setDisplayName(role.coloredDisplayName)
            lore = listOf("${ChatColor.GRAY}Rolle")
        })

        setItems()

        corpse = CorpseAPI.spawnCorpse(player, location)

        updateTimeListener = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            fullMinutesSinceDeath += 1
            setTimeItem()
        }, secondsToTicks(60).toLong(), secondsToTicks(60).toLong())
    }

    private fun setTimeItem() {
        if (status == Status.INSPECTED) {
            inventory.setItem(TIME_SLOT, ItemStack(ResourcePack.Items.time).applyMeta {
                val timeString =
                    if (fullMinutesSinceDeath == 0) "Vor weniger als einer Minute"
                    else "Vor weniger als ${fullMinutesSinceDeath + 1} Minuten"

                setDisplayName("${ChatColor.RESET}$timeString")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        } else {
            inventory.setItem(TIME_SLOT, ItemStack(ResourcePack.Items.time).applyMeta {
                setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        }
    }

    private fun setReasonItem() {
        if (status == Status.INSPECTED) {
            val reasonItemStack = if (reason is DeathReason.Item) reason.item.itemStack.clone() else ItemStack(ResourcePack.Items.deathReason)
            inventory.setItem(REASON_SLOT, reasonItemStack.applyMeta {
                setDisplayName("${ChatColor.RESET}" + reason.displayText)
                lore = listOf("${ChatColor.GRAY}Grund des Todes")
            })
        } else {
            inventory.setItem(REASON_SLOT, ItemStack(ResourcePack.Items.questionMark).applyMeta {
                setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
                lore = listOf("${ChatColor.GRAY}Grund des Todes")
            })
        }
    }

    private fun setItems() {
        setTimeItem()
        setReasonItem()
    }

    fun identify(tttPlayer: TTTPlayer, inspect: Boolean) {
        if (status == Status.UNIDENTIFIED) {
            GameMessenger.corpseIdentified(tttPlayer.player.displayName, player.displayName, role)

            if (!inspect) {
                status = Status.IDENTIFIED
            }
        }

        if (inspect && status != Status.INSPECTED) {
            status = Status.INSPECTED
            setItems()
        }

        if (credits != 0 && tttPlayer.role.canOwnCredits) {
            val c = credits
            credits = 0
            tttPlayer.credits += c

            if (c > 1) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.GREEN}Du hast $c Credits aufgesammelt")
            } else  {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.GREEN}Du hast 1 Credit aufgesammelt")
            }
        }
    }

    fun destroy() {
        if (corpse !== null) CorpseAPI.removeCorpse(corpse)
        updateTimeListener.cancel()
        inventory.viewers.toSet().forEach { it.closeInventory() }
    }

    enum class Status {
        UNIDENTIFIED,
        IDENTIFIED,
        INSPECTED
    }

    companion object {
        private const val ROLE_SLOT = 0
        private const val REASON_SLOT = 1
        private const val TIME_SLOT = 2

        fun spawn(tttPlayer: TTTPlayer, reason: DeathReason) {
            CorpseManager.add(TTTCorpse(
                tttPlayer.player,
                tttPlayer.player.location,
                tttPlayer.role,
                reason,
                tttPlayer.credits
            ))
        }
    }
}
