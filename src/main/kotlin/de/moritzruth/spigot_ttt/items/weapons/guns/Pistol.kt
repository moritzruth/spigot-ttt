package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Pistol: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Pistol",
    damage = heartsToHealth(2.5),
    cooldown = 0.8,
    magazineSize = 10,
    reloadTime = 2.0,
    itemMaterial = CustomItems.pistol
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


