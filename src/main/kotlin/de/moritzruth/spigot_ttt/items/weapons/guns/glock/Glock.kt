package de.moritzruth.spigot_ttt.items.weapons.guns.glock

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

object Glock: Gun<GlockState>() {
    override val spawning = true
    override val displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Glock"
    override val damage = heartsToHealth(1.5)
    override val cooldown = 0.3
    override val magazineSize = 20
    override val reloadTime = 2.0
    override val itemMaterial = CustomItems.glock
    override val itemStack = ItemStack(itemMaterial).apply {
        itemMeta = getItemMeta(this)
    }
    override val recoil = 2
    override val type = TTTItem.Type.PISTOL_LIKE

    override fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(GlockState::class) { GlockState() }
}


