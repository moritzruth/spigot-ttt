package de.moritzruth.spigot_ttt.items.weapons.baseball_bat

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.JACKAL
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.SelectableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

object BaseballBat: TTTItem, BuyableItem, SelectableItem {
    override val displayName = "${ChatColor.RESET}${ChatColor.BOLD}Baseball-Schläger"
    override val spawning = false
    override val type = TTTItem.Type.SPECIAL_WEAPON
    override val itemStack = ItemStack(CustomItems.baseballBat)
    override val listener = BaseballBatListener
    override val buyableBy = EnumSet.of(TRAITOR, JACKAL)
    override val price = 1

    init {
        val meta = itemStack.itemMeta!!

        meta.setDisplayName(displayName)
        meta.lore = listOf(
            "",
            "${ChatColor.GOLD}Erhöht die Gechwindigkeit",
            "${ChatColor.GOLD}Schleudert den Gegner weg",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        itemStack.itemMeta = meta
    }

    override fun onSelect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.3F
    }

    override fun onDeselect(tttPlayer: TTTPlayer) {
        tttPlayer.player.walkSpeed = 0.2F
    }
}
