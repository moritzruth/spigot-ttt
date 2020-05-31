package de.moritzruth.spigot_ttt.game.players.corpses

import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.CustomItems
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI
import org.golde.bukkit.corpsereborn.nms.Corpses
import java.time.Instant

class TTTCorpse(private val player: Player, location: Location, private val role: TTTPlayer.Role, private val reason: DeathReason) {
    val corpse: Corpses.CorpseData?
    val inventory = player.server.createInventory(null, InventoryType.HOPPER, "${role.chatColor}${player.displayName}")
    var identified = false
    val timestamp: Instant = Instant.now()

    init {
        inventory.setItem(0, ItemStack(role.iconItemMaterial).apply {
            val meta = itemMeta!!
            meta.setDisplayName(role.coloredDisplayName)
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            meta.lore = listOf("${ChatColor.GRAY}Rolle")

            itemMeta = meta
        })

        val reasonItem = if (reason is DeathReason.Item) reason.item.itemStack.clone() else ItemStack(CustomItems.deathReason)
        reasonItem.itemMeta = reasonItem.itemMeta!!.apply {
            setDisplayName("${ChatColor.RESET}" + reason.displayText)
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            lore = listOf("${ChatColor.GRAY}Grund des Todes")
        }

        inventory.setItem(1, reasonItem)

        corpse = CorpseAPI.spawnCorpse(player, location)
    }

    fun destroy() {
        if (corpse !== null) CorpseAPI.removeCorpse(corpse)
    }

    fun identify(by: Player) {
        if (identified) return

        GameMessenger.corpseIdentified(by.displayName, player.displayName, role)
        identified = true
    }
}
