package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.Probability
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object EnderPearl : TTTItem<EnderPearl.Instance>(
    instanceType = Instance::class,
    type = Type.SPECIAL,
    templateItemStack = ItemStack(Material.ENDER_PEARL).applyMeta {
        setDisplayName("${ChatColor.DARK_GREEN}Enderperle")
    },
    spawnProbability = Probability.VERY_LOW,
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL, Role.DETECTIVE),
        price = 1
    )
) {
    class Instance: TTTItem.Instance(EnderPearl)
}
