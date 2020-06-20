package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask

object TreeGun: Gun(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    shopInfo = ShopInfo(
        buyableBy = roles(Role.JACKAL, Role.TRAITOR),
        buyLimit = 2,
        price = 1
    ),
    displayName = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}Tree Gun",
    itemLore = listOf(
        "",
        "${ChatColor.GOLD}Verwandelt einen Spieler nach",
        "${ChatColor.GOLD}kurzer Zeit in einen Baum",
        "",
        "${ChatColor.RED}Nur ein Schuss"
    ),
    appendLore = false,
    damage = 0.1, // Not really
    cooldown = 1.0,
    magazineSize = 1,
    reloadTime = 0.0,
    material = Resourcepack.Items.treeGun,
    shootSound = Resourcepack.Sounds.Item.Weapon.Pistol.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Pistol.reload
) {
    private val transmutingPlayers = mutableSetOf<TransmutingPlayer>()

    data class TransmutingPlayer(
        val tttPlayer: TTTPlayer,
        val killer: TTTPlayer,
        val task: BukkitTask
    )

    override fun onReset() {
        transmutingPlayers.forEach { it.task.cancel() }
    }

    private fun spawnTree(tttPlayer: TTTPlayer) {
        val centerLocation = tttPlayer.player.location

        for (y in 0..3) {
            centerLocation.clone().add(0.0, y.toDouble(), 0.0).block.type = Material.OAK_WOOD
        }

        for (y in 2..3) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (z == 0 && x == 0) continue

                    centerLocation.clone()
                        .add(x.toDouble(), y.toDouble(), z.toDouble())
                        .block.type = Material.OAK_LEAVES
                }
            }
        }

        centerLocation.clone().add(0.0, 4.0, 0.0).block.type = Material.OAK_LEAVES

        val signBlock = centerLocation.clone().add(1.0, 1.0, 0.0).block
        signBlock.type = Material.OAK_WALL_SIGN
        val data = signBlock.blockData as WallSign
        data.facing = BlockFace.EAST
        signBlock.blockData = data

        val state = signBlock.state as Sign
        state.setLine(1, "${ChatColor.BLACK}${ChatColor.BOLD}R.I.P.")
        state.setLine(2, tttPlayer.player.name)
        state.update()
    }

    fun startTransmuting(tttPlayer: TTTPlayer, killer: TTTPlayer) {
        transmutingPlayers.add(TransmutingPlayer(
            tttPlayer,
            killer,
            plugin.server.scheduler.runTaskLater(plugin, fun() {
                spawnTree(tttPlayer)
                tttPlayer.damage(
                    1000.0,
                    DeathReason.Item(TreeGun),
                    killer,
                    scream = false,
                    spawnCorpse = false
                )
            }, secondsToTicks(6).toLong())
        ))

        tttPlayer.player.addPotionEffect(PotionEffect(
            PotionEffectType.SLOW,
            1000000,
            3,
            false,
            false
        ))
    }

    class Instance: Gun.Instance(TreeGun) {
        override fun reload() {
            requireCarrier().player.sendActionBarMessage("${ChatColor.RED}Du kannst diese Waffe nicht nachladen")
        }

        override fun onHit(tttPlayer: TTTPlayer, hitTTTPlayer: TTTPlayer) {
            startTransmuting(hitTTTPlayer, tttPlayer)
        }
    }
}


