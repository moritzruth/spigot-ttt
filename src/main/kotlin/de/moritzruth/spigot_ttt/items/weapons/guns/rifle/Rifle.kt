package de.moritzruth.spigot_ttt.items.weapons.guns.pistol

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import java.util.*

object Rifle: Gun<RifleState>(), BuyableItem {
    override val spawning = true
    override val displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Rifle"
    override val damage = heartsToHealth(0.5)
    override val cooldown = 0.1
    override val magazineSize = 40
    override val reloadTime = 2.0
    override val itemMaterial = CustomItems.rifle
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this)
    }
    override val recoil = 1
    override val type = TTTItem.Type.HEAVY_WEAPON
    override val price = 1
    override val buyableBy = EnumSet.of(TTTPlayer.Role.TRAITOR)

    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(RifleState::class) { RifleState() }
}


