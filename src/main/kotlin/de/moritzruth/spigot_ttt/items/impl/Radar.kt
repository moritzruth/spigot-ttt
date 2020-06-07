package de.moritzruth.spigot_ttt.items.impl

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

object Radar: TTTItem, Buyable {
    private val DISPLAY_NAME = "${ChatColor.DARK_AQUA}${ChatColor.BOLD}Radar"
    private const val ACTIVE_DURATION = 10
    private const val COOLDOWN_DURATION = 40

    override val itemStack = ItemStack(ResourcePack.Items.radar).applyMeta {
        setDisplayName(DISPLAY_NAME)
        lore = listOf(
            "",
            "${ChatColor.GOLD}Zeigt dir alle 30 Sekunden",
            "${ChatColor.GOLD}für 10 Sekunden die Positionen",
            "${ChatColor.GOLD}aller Spieler"
        )

        hideInfo()
    }
    override val type = TTTItem.Type.SPECIAL
    override val buyableBy: EnumSet<Role> = EnumSet.of(Role.TRAITOR, Role.DETECTIVE, Role.JACKAL)
    override val price = 2
    override val buyLimit: Int? = null

    val isc = InversedStateContainer(State::class)

    fun use(tttPlayer: TTTPlayer, itemStack: ItemStack) {
        val state = isc.getOrCreate(tttPlayer)
        if (state.enabled) return
        state.enabled = true

        state.bossBar = plugin.server.createBossBar(DISPLAY_NAME, BarColor.BLUE, BarStyle.SOLID)
        state.bossBar.addPlayer(tttPlayer.player)

        setActive(tttPlayer, true)
        state.task = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            val duration = Duration.between(state.timestamp, Instant.now()).toMillis().toDouble() / 1000

            if (state.active) {
                if (duration > ACTIVE_DURATION) {
                    setActive(tttPlayer, false)
                } else {
                    state.bossBar.progress = 1.0 - duration / ACTIVE_DURATION
                }
            } else {
                if (duration > COOLDOWN_DURATION) {
                    setActive(tttPlayer, true)
                } else {
                    state.bossBar.progress = duration / COOLDOWN_DURATION
                }
            }
        }, 0, 2)

        itemStack.applyMeta {
            setDisplayName(DISPLAY_NAME + "${ChatColor.RESET} - ${ChatColor.GREEN}Aktiviert")
        }
    }

    private fun setActive(tttPlayer: TTTPlayer, value: Boolean) {
        val state = isc.getOrCreate(tttPlayer)

        if (state.active != value) {
            state.active = value
            state.timestamp = Instant.now()

            if (value) {
                state.bossBar.setTitle(DISPLAY_NAME + "${ChatColor.WHITE} - ${ChatColor.GREEN}Aktiv")
            } else {
                state.bossBar.setTitle(DISPLAY_NAME + "${ChatColor.WHITE} - ${ChatColor.GRAY}Cooldown")
            }

            // Toggle sending the entity metadata
            PlayerManager.tttPlayers.forEach {
                if (it !== tttPlayer) {
                    tttPlayer.player.hidePlayer(plugin, it.player)
                    tttPlayer.player.showPlayer(plugin, it.player)
                }
            }
        }
    }

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(Radar)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Radar)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.action.isRightClick) {
                use(tttPlayer, event.item!!)
            }

            event.isCancelled = true
        }
    }

    override val packetListener = object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
        override fun onPacketSending(event: PacketEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            val packet = WrapperPlayServerEntityMetadata(event.packet)

            val playerOfPacket = plugin.server.onlinePlayers.find { it.entityId == packet.entityID } ?: return
            val tttPlayerOfPacket = PlayerManager.getTTTPlayer(playerOfPacket) ?: return
            if (tttPlayerOfPacket.alive) {
                // https://wiki.vg/Entity_metadata#Entity_Metadata_Format
                try {
                    val modifiers = packet.metadata[0].value as Byte
                    packet.metadata[0].value = if (isc.get(tttPlayer)?.active == true) modifiers or 0x40
                    else modifiers and 0b10111111.toByte()
                } catch (ignored: Exception) {
                    // Idk why this throws exceptions, but it works anyways
                }
            }
        }
    }

    class State: IState {
        var enabled = false
        var task: BukkitTask? = null
        var active: Boolean = false
        lateinit var timestamp: Instant
        lateinit var bossBar: BossBar

        override fun reset(tttPlayer: TTTPlayer) {
            setActive(tttPlayer, false)

            task?.cancel()
            bossBar.removePlayer(tttPlayer.player)
        }
    }
}
