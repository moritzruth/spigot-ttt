package de.moritzruth.spigot_ttt.game.items.impl

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant
import kotlin.experimental.and
import kotlin.experimental.or

object Radar: TTTItem<Radar.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.radar).applyMeta {
        setDisplayName("${ChatColor.DARK_AQUA}${ChatColor.BOLD}Radar$PASSIVE_SUFFIX")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Zeigt dir alle 30 Sekunden",
            "${ChatColor.GOLD}für 10 Sekunden die Positionen",
            "${ChatColor.GOLD}aller Spieler"
        )

        hideInfo()
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.DETECTIVE, Role.JACKAL),
        price = 2
    )
) {
    private const val ACTIVE_DURATION = 10
    private const val COOLDOWN_DURATION = 40

    class Instance: TTTItem.Instance(Radar) {
        var active: Boolean = true
        private var timestamp = Instant.now()!!
        private val bossBar = plugin.server.createBossBar(ACTIVE_TITLE, BarColor.BLUE, BarStyle.SOLID)

        private var task: BukkitTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            val duration = Duration.between(timestamp, Instant.now()).toMillis().toDouble() / 1000

            if (active) {
                if (duration > ACTIVE_DURATION) {
                    active = false
                    bossBar.setTitle(COOLDOWN_TITLE)
                    carrier?.let { resendEntityMetadata(it) }
                    timestamp = Instant.now()
                } else {
                    bossBar.progress = 1.0 - duration / ACTIVE_DURATION
                }
            } else {
                if (duration > COOLDOWN_DURATION) {
                    active = true
                    bossBar.setTitle(ACTIVE_TITLE)
                    carrier?.let { resendEntityMetadata(it) }
                    timestamp = Instant.now()
                } else {
                    bossBar.progress = duration / COOLDOWN_DURATION
                }
            }
        }, 0, 1)

        override fun onCarrierSet(carrier: TTTPlayer, isFirst: Boolean) {
            bossBar.addPlayer(carrier.player)
            if (active) resendEntityMetadata(carrier)
        }

        override fun onCarrierRemoved(oldCarrier: TTTPlayer) {
            bossBar.removePlayer(oldCarrier.player)
            if (active) resendEntityMetadata(oldCarrier)
        }

        override fun reset() {
            task.cancel()
            active = false
            carrier?.let { resendEntityMetadata(it) }
        }

        companion object {
            private val BOSS_BAR_TITLE = "${ChatColor.DARK_AQUA}${ChatColor.BOLD}Radar"
            private val ACTIVE_TITLE = BOSS_BAR_TITLE + "${ChatColor.WHITE} - ${ChatColor.GREEN}Aktiv"
            private val COOLDOWN_TITLE = BOSS_BAR_TITLE + "${ChatColor.WHITE} - ${ChatColor.GRAY}Cooldown"
        }
    }

    fun resendEntityMetadata(tttPlayer: TTTPlayer) {
        PlayerManager.tttPlayers.forEach {
            if (it !== tttPlayer) {
                tttPlayer.player.hidePlayer(plugin, it.player)
                tttPlayer.player.showPlayer(plugin, it.player)
            }
        }
    }

    init {
        addListener(object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(event: PacketEvent) {
                val receivingTTTPlayer = TTTPlayer.of(event.player) ?: return

                val packet = WrapperPlayServerEntityMetadata(event.packet)
                val tttPlayerOfPacket = plugin.server.onlinePlayers
                    .find { it.entityId == packet.entityID }
                    ?.let { TTTPlayer.of(it) } ?: return
                val instance = getInstance(receivingTTTPlayer) ?: return

                if (tttPlayerOfPacket.alive) {
                    // https://wiki.vg/Entity_metadata#Entity_Metadata_Format
                    try {
                        val modifiers = packet.metadata[0].value as Byte
                        packet.metadata[0].value =
                            if (instance.active) modifiers or 0x40.toByte()
                            else modifiers and 0b10111111.toByte()
                    } catch (ignored: Exception) {
                        // Idk why this throws exceptions, but it works anyways
                    }
                }
            }
        })
    }
}
