package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.Spawning
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Glock: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Glock",
    damage = heartsToHealth(1.5),
    cooldown = 0.3,
    magazineSize = 20,
    reloadTime = 2.0,
    itemMaterial = Resourcepack.Items.glock,
    shootSound = Resourcepack.Sounds.Item.Weapon.Glock.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Glock.reload
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


