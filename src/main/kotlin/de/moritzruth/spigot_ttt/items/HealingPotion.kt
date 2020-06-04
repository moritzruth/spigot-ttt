package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.*
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import java.util.*

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
    override val buyableBy = EnumSet.of(TRAITOR, JACKAL, SIDEKICK, DETECTIVE)
    override val price = 1

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(HealingPotion)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
            PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.item.isSimilar(itemStack)) {
                event.isCancelled = true
                event.player.inventory.clear(event.player.inventory.indexOf(event.item))
                event.player.health = event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 100.0
            }
        }
    }
}
