package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object HealingPotion: TTTItem, Buyable {
    override val itemStack = ItemStack(Material.POTION).apply {
        val potionMeta = itemMeta as PotionMeta
        potionMeta.basePotionData = PotionData(PotionType.INSTANT_HEAL, false, true)
        itemMeta = potionMeta
    }.applyMeta {
        setDisplayName("${ChatColor.LIGHT_PURPLE}Heiltrank")
        lore = listOf(
                "",
                "${ChatColor.GOLD}Heilt dich voll"
        )

        addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
    }
    override val type = TTTItem.Type.SPECIAL
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL, Role.DETECTIVE)
    override val price = 1
    override val buyLimit = 2

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onPlayerItemConsume(event: PlayerItemConsumeEvent) = handle(event) {
            event.isCancelled = true
            event.player.inventory.clear(event.player.inventory.indexOf(event.item))
            event.player.health = event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 100.0
        }

        override fun onRightClick(data: ClickEventData) {
            data.event.isCancelled = false
        }
    }
}
