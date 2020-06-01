package de.moritzruth.spigot_ttt.items.weapons.guns.deagle

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.DETECTIVE
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object GoldenDeagle: Gun<GoldenDeagleState>(), BuyableItem {
    override val spawning = false
    override val displayName = "${ChatColor.GOLD}${ChatColor.BOLD}Golden Deagle"
    override val damage = 1.0 // is not used
    override val cooldown = 1.4
    override val magazineSize = 2
    override val reloadTime = 10.0
    override val itemMaterial = CustomItems.deagle
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this).apply {
            lore = listOf(
                "",
                "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(null)}",
                "${ChatColor.GRAY}Cooldown: ${LoreHelper.cooldown(cooldown)}",
                "${ChatColor.GRAY}Magazin: ${LoreHelper.uses(magazineSize)} Schuss"
            )
        }
    }
    override val recoil = 10
    override val buyableBy: EnumSet<TTTPlayer.Role> = EnumSet.of(TRAITOR, DETECTIVE)
    override val price = 3
    override val type = TTTItem.Type.PISTOL_LIKE

    override fun computeActualDamage(tttPlayer: TTTPlayer, receiver: Player) = 1000.0
    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(GoldenDeagleState::class) { GoldenDeagleState() }
}


