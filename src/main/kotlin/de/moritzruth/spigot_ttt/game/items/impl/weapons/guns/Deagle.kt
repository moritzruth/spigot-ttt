package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.utils.Probability
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Deagle: Gun(
    type = Type.PISTOL_LIKE,
    instanceType = Instance::class,
    spawnProbability = Probability.NORMAL,
    displayName = "${ChatColor.BLUE}${ChatColor.BOLD}Deagle",
    damage = heartsToHealth(3.0),
    cooldown = 1.4,
    magazineSize = 8,
    reloadTime = 3.0,
    material = Resourcepack.Items.deagle,
    shootSound = Resourcepack.Sounds.Item.Weapon.Deagle.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Deagle.reload
) {
    class Instance: Gun.Instance(Deagle)
}


