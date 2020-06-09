package de.moritzruth.spigot_ttt

import org.bukkit.Material

object ResourcePack {
    private const val NAMESPACE = "ttt:"

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

        // Special Item
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
        const val playerDeath = "${NAMESPACE}player_death"

        object Item {
            private const val PREFIX = NAMESPACE + "item."

            object Weapon {
                private const val PREFIX = Item.PREFIX + "weapon."

                object Pistol {
                    private const val PREFIX = Weapon.PREFIX + "pistol."

                    const val fire = "${PREFIX}fire"
                    const val reload = "${PREFIX}reload"
                }

                object Glock {
                    private const val PREFIX = Weapon.PREFIX + "glock."

                    const val fire = "${PREFIX}fire"
                    const val reload = "${PREFIX}reload"
                }

                object Deagle {
                    private const val PREFIX = Weapon.PREFIX + "deagle."

                    const val fire = "${PREFIX}fire"
                    const val reload = "${PREFIX}reload"
                }

                object Shotgun {
                    private const val PREFIX = Weapon.PREFIX + "shotgun."

                    const val fire = "${PREFIX}fire"
                    const val reload = "${PREFIX}reload"
                }

                object Rifle {
                    private const val PREFIX = Weapon.PREFIX + "rifle."

                    const val fire = "${PREFIX}fire"
                    const val reload = "${PREFIX}reload"
                }

                object Generic {
                    private const val PREFIX = Weapon.PREFIX + "generic."

                    const val emptyMagazine = "${PREFIX}empty_magazine"
                }
            }

            object CloakingDevice {
                private const val PREFIX = Item.PREFIX + "cloaking_device."

                const val on = "${PREFIX}on"
                const val off = "${PREFIX}off"
            }

            object BaseballBat {
                private const val PREFIX = Item.PREFIX + "baseball_bat."

                const val hit = "${PREFIX}hit"
            }

            object Knife {
                private const val PREFIX = Item.PREFIX + "knife."

                const val hit = "${PREFIX}hit"
            }
        }
    }
}
