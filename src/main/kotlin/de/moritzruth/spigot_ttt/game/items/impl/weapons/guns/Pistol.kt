package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.items.Spawning
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Pistol: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Pistol",
    damage = heartsToHealth(2.5),
    cooldown = 0.8,
    magazineSize = 10,
    reloadTime = 2.0,
    itemMaterial = ResourcePack.Items.pistol,
    shootSound = ResourcePack.Sounds.Item.Weapon.Pistol.fire,
    reloadSound = ResourcePack.Sounds.Item.Weapon.Pistol.reload
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}

