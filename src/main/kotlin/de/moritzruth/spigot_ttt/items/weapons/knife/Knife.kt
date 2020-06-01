package de.moritzruth.spigot_ttt.items.weapons.knife

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

object Knife: TTTItem, BuyableItem {
    override val displayName = "${ChatColor.RED}${ChatColor.BOLD}Knife"
    override val spawning = false
    override val listener = KnifeListener
    override val itemStack = ItemStack(CustomItems.knife)
    override val buyableBy: EnumSet<TTTPlayer.Role> = EnumSet.of(TRAITOR)
    override val price = 1
    override val type = TTTItem.Type.MELEE

    init {
        val meta = itemStack.itemMeta!!

        meta.setDisplayName(displayName)
        meta.lore = listOf(
                "",
                "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(null)}",
                "",
                "${ChatColor.RED}Nur einmal verwendbar",
                "${ChatColor.RED}Nur aus nächster Nähe"
        )

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        itemStack.itemMeta = meta
    }
}


