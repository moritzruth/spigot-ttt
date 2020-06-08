package de.moritzruth.spigot_ttt

import org.bukkit.Material

object ResourcePack {
    object Items {
        val textureless = Material.WHITE_STAINED_GLASS_PANE
        val deathReason = Material.GRAY_STAINED_GLASS_PANE
        val questionMark = Material.GRASS_BLOCK
        val time = Material.CLOCK
        val dot = Material.GRAY_STAINED_GLASS
        val arrowDown = Material.WHITE_STAINED_GLASS

        // Roles
        val innocent = Material.GREEN_STAINED_GLASS_PANE
        val detective = Material.YELLOW_STAINED_GLASS_PANE
        val traitor = Material.RED_STAINED_GLASS_PANE
        val jackal = Material.LIGHT_BLUE_STAINED_GLASS_PANE
        val sidekick = Material.BLUE_STAINED_GLASS_PANE

        // Special Items
        val cloakingDevice = Material.COBWEB
        val radar = Material.IRON_HELMET
        val teleporter = Material.SPRUCE_WOOD
        val martyrdomGrenade = Material.BIRCH_LOG
        val fakeCorpse = Material.SPRUCE_LOG
        val defibrillator = Material.IRON_INGOT
        val secondChance = Material.GOLD_INGOT

        // Weapons
        val deagle = Material.IRON_HOE
        val sidekickDeagle = Material.GOLDEN_HOE
        val glock = Material.STONE_HOE
        val pistol = Material.WOODEN_HOE
        val shotgun = Material.IRON_AXE
        val knife = Material.IRON_SWORD
        val baseballBat = Material.STICK
        val rifle = Material.DIAMOND_HOE
    }

    object Sounds {
        const val error = "minecraft:block.anvil.break"
        const val grenadeExplode = "minecraft:entity.generic.explode"
    }
}
