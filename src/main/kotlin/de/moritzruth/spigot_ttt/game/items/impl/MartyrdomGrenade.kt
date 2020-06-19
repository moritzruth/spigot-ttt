package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
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
    )
) {
    class Instance: TTTItem.Instance(MartyrdomGrenade, true) {
        var explodeTask: BukkitTask? = null

        override fun reset() {
            explodeTask?.cancel()
            explodeTask = null
        }
    }

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler
        fun onTTTPlayerTrueDeath(event: TTTPlayerTrueDeathEvent) {
            val instance = getInstance(event.tttPlayer) ?: return
            event.tttPlayer.removeItem(MartyrdomGrenade, false)

            instance.explodeTask = plugin.server.scheduler.runTaskLater(plugin, fun() {
                GameManager.world.playSound(
                    event.location,
                    Resourcepack.Sounds.grenadeExplode,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                createKillExplosion(event.tttPlayer, event.location, 2.5)
            }, secondsToTicks(3).toLong())
        }
    }
}
