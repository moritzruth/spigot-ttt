package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayerTrueDeathEvent
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

object MartyrdomGrenade: TTTItem<MartyrdomGrenade.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.martyrdomGrenade).applyMeta {
        hideInfo()
        setDisplayName("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Märtyriumsgranate$PASSIVE_SUFFIX")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Lasse bei deinem Tod",
            "${ChatColor.GOLD}eine Granate fallen"
        )
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        price = 1
    ),
    removeInstanceOnDeath = false
) {
    class Instance: TTTItem.Instance(MartyrdomGrenade) {
        var explodeTask: BukkitTask? = null
        var tttPlayer: TTTPlayer? = null

        override fun reset() {
            explodeTask?.cancel()
            explodeTask = null
        }
    }

    init {
        addListener(object : TTTItemListener<Instance>(this) {
            @EventHandler
            fun onTTTPlayerTrueDeath(event: TTTPlayerTrueDeathEvent) {
                val instance = getInstance(event.tttPlayer) ?: return
                instance.tttPlayer = event.tttPlayer

                instance.explodeTask = plugin.server.scheduler.runTaskLater(plugin, fun() {
                    GameManager.world.playSound(
                        event.location,
                        Resourcepack.Sounds.grenadeExplode,
                        SoundCategory.PLAYERS,
                        1F,
                        1F
                    )

                    createKillExplosion(event.tttPlayer, event.location, 5.0)
                    instance.remove()
                }, secondsToTicks(3).toLong())
            }
        })
    }
}
