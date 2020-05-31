package de.moritzruth.spigot_ttt.items.weapons.guns.deagle

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.DETECTIVE
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import java.util.*

object Deagle: Gun<DeagleState>(), BuyableItem {
    override val spawning = true
    override val displayName = "${ChatColor.BLUE}${ChatColor.BOLD}Deagle"
    override val damage = heartsToHealth(5.0)
    override val cooldown = 1.4
    override val magazineSize = 8
    override val reloadTime = 3.0
    override val itemMaterial = CustomItems.deagle
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this)
    }
    override val recoil = 10
    override val buyableBy: EnumSet<TTTPlayer.Role> = EnumSet.of(TRAITOR, DETECTIVE)
    override val price = 1
    override val type = TTTItem.Type.NORMAL_WEAPON

    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(DeagleState::class) { DeagleState() }
}


