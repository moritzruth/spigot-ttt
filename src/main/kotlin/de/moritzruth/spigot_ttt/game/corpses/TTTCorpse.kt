package de.moritzruth.spigot_ttt.game.corpses

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
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

class TTTCorpse(private val player: Player, location: Location, private val role: Role, private val reason: DeathReason) {
    val corpse: Corpses.CorpseData?
    val inventory = player.server.createInventory(null, InventoryType.HOPPER, "${role.chatColor}${player.displayName}")
    val timestamp: Instant = Instant.now()
    private var identified = false
    private var inspected = false
        private set(value) {
            field = value
            updateTimeItem()
        }
    private var wholeMinutesSinceDeath = 0; private set
    private var updateTimeListener: BukkitTask

    init {
        inventory.setItem(ROLE_SLOT, ItemStack(role.iconItemMaterial).applyMeta {
            setDisplayName(role.coloredDisplayName)
            lore = listOf("${ChatColor.GRAY}Rolle")
        })

        inventory.setItem(REASON_SLOT, ItemStack(CustomItems.questionMark).applyMeta {
            setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
            lore = listOf("${ChatColor.GRAY}Grund des Todes")
        })

        updateTimeItem()

        corpse = CorpseAPI.spawnCorpse(player, location)

        updateTimeListener = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            wholeMinutesSinceDeath += 1
            updateTimeItem()
        }, secondsToTicks(60).toLong(), secondsToTicks(60).toLong())
    }

    private fun updateTimeItem() {
        if (inspected) {
            inventory.setItem(TIME_SLOT, ItemStack(CustomItems.time).applyMeta {
                val timeString =
                        if (wholeMinutesSinceDeath == 0) "Vor weniger als einer Minute"
                        else "Vor weniger als ${wholeMinutesSinceDeath + 1} Minuten"

                setDisplayName("${ChatColor.RESET}$timeString")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        } else {
            inventory.setItem(TIME_SLOT, ItemStack(CustomItems.time).applyMeta {
                setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}##########")
                lore = listOf("${ChatColor.GRAY}Zeit des Todes")
            })
        }
    }

    fun inspect(player: Player) {
        identify(player)
        inspected = true

        val reasonItem = if (reason is DeathReason.Item) reason.item.itemStack.clone() else ItemStack(CustomItems.deathReason)
        inventory.setItem(REASON_SLOT, reasonItem.applyMeta {
            setDisplayName("${ChatColor.RESET}" + reason.displayText)
            lore = listOf("${ChatColor.GRAY}Grund des Todes")
        })
    }

    fun destroy() {
        if (corpse !== null) CorpseAPI.removeCorpse(corpse)
        updateTimeListener.cancel()
        inventory.viewers.forEach { it.closeInventory() }
    }

    fun identify(by: Player) {
        if (identified) return

        GameMessenger.corpseIdentified(by.displayName, player.displayName, role)
        identified = true
    }

    companion object {
        const val ROLE_SLOT = 0
        const val REASON_SLOT = 1
        const val TIME_SLOT = 2
    }
}
