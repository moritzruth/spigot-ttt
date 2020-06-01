package de.moritzruth.spigot_ttt.items.weapons.guns.pistol

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

object Pistol: Gun<PistolState>() {
    override val spawning = true
    override val displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Pistol"
    override val damage = heartsToHealth(2.5)
    override val cooldown = 0.8
    override val magazineSize = 10
    override val reloadTime = 2.0
    override val itemMaterial = CustomItems.pistol
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this)
    }
    override val recoil = 5
    override val type = TTTItem.Type.PISTOL_LIKE

    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(PistolState::class) { PistolState() }
}


