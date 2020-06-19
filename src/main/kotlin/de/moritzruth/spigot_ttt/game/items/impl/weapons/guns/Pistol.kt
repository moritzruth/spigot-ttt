package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.utils.Probability
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Pistol: Gun(
    type = Type.PISTOL_LIKE,
    instanceType = Instance::class,
    spawnProbability = Probability.NORMAL,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Pistol",
    damage = heartsToHealth(2.5),
    cooldown = 0.8,
    magazineSize = 10,
    reloadTime = 2.0,
    material = Resourcepack.Items.pistol,
    shootSound = Resourcepack.Sounds.Item.Weapon.Pistol.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Pistol.reload
) {
    class Instance: Gun.Instance(Pistol)
}


