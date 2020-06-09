package de.moritzruth.spigot_ttt.items.weapons.guns.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Glock: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Glock",
    damage = heartsToHealth(1.5),
    cooldown = 0.3,
    magazineSize = 20,
    reloadTime = 2.0,
    itemMaterial = ResourcePack.Items.glock,
    shootSound = ResourcePack.Sounds.Item.Weapon.Glock.fire,
    reloadSound = ResourcePack.Sounds.Item.Weapon.Glock.reload
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


