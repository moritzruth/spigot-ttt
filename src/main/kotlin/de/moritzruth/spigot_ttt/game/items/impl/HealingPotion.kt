package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.game.items.SpawnProbability
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.applyTypedMeta
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

object HealingPotion: TTTItem<HealingPotion.Instance>(
    instanceType = Instance::class,
    type = Type.SPECIAL,
    spawnProbability = SpawnProbability.VERY_LOW,
    templateItemStack = ItemStack(Material.POTION)
        .applyTypedMeta<PotionMeta> { basePotionData = PotionData(PotionType.INSTANT_HEAL, false, true) }
        .applyMeta {
            setDisplayName("${ChatColor.LIGHT_PURPLE}Heiltrank")
            lore = listOf(
                "",
                "${ChatColor.GOLD}Heilt dich voll"
            )

            addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL, Role.DETECTIVE),
        price = 1,
        buyLimit = 2
    )
) {
    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler
        fun onPlayerItemConsume(event: PlayerItemConsumeEvent) = handle(event) {
            event.isCancelled = true
            event.player.inventory.clear(event.player.inventory.indexOf(event.item))
            event.player.health = event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 100.0
        }
    }

    class Instance: TTTItem.Instance(HealingPotion)
}
