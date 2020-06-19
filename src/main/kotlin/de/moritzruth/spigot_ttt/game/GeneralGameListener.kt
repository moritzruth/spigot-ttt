package de.moritzruth.spigot_ttt.game

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.call
import de.moritzruth.spigot_ttt.utils.nextTick
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import java.util.*

object GeneralGameListener : Listener {
    private val BLOCKED_COMMANDS = setOf("me", "tell", "msg")

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) = PlayerManager.onPlayerJoin(event.player)

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) = PlayerManager.onPlayerQuit(event.player)

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (event.message.startsWith("/rl") && GameManager.phase != null) { // /reload is not blocked
            event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}The server may not be reloaded while the game is running")
            event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}You can force reload by using ${ChatColor.WHITE}/reload")
            event.isCancelled = true
            return
        }

        if (BLOCKED_COMMANDS.find { event.message.startsWith("/$it") } != null) {
            if (GameManager.phase != null) {
                event.player.sendMessage(TTTPlugin.prefix + "${ChatColor.RED}Dieser Befehl ist blockiert.")
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player = event.damager
        if (player is Player) {
            if (player.inventory.itemInMainHand.type == Material.AIR) {
                event.damage = 0.2
            }
        }
    }

    private val ZERO_NO_DAMAGE_TICKS_CAUSES = EnumSet.of(
        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
        EntityDamageEvent.DamageCause.CUSTOM,
        EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
        EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
        EntityDamageEvent.DamageCause.FALL,
        EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
        EntityDamageEvent.DamageCause.FALLING_BLOCK,
        EntityDamageEvent.DamageCause.SUICIDE
    )!!

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityDamageLow(event: EntityDamageEvent) {
        if (ZERO_NO_DAMAGE_TICKS_CAUSES.contains(event.cause)) {
            val player = event.entity
            if (player is Player) {
                nextTick { player.noDamageTicks = 0 }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageHighest(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val tttPlayer = TTTPlayer.of(event.entity as Player) ?: return
        if (event.cause == EntityDamageEvent.DamageCause.CUSTOM) return

        val reason = when (event.cause) {
            EntityDamageEvent.DamageCause.FALL -> DeathReason.FALL
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> DeathReason.EXPLOSION
            EntityDamageEvent.DamageCause.DROWNING -> DeathReason.DROWNED
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.HOT_FLOOR -> DeathReason.FIRE
            EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.WITHER -> DeathReason.POISON
            else -> DeathReason.SUICIDE
        }

        val e = TTTPlayerDamageEvent(tttPlayer, event.finalDamage, reason).call()

        if (tttPlayer.player.health - e.damage <= 0) {
            tttPlayer.onDeath(reason, null)
            event.damage = 0.0
        } else {
            event.damage = e.damage
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage = null
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player.inventory.itemInMainHand.type == Material.AIR && event.action == Action.LEFT_CLICK_BLOCK) {
            GameManager.destroyBlock(event.clickedBlock!!)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerSwapHandItemsLowest(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val senderTTTPlayer = TTTPlayer.of(event.player) ?: return

        if (!senderTTTPlayer.alive) {
            PlayerManager.tttPlayers.filter { !it.alive }.forEach {
                it.player.sendMessage("${ChatColor.GRAY}[${ChatColor.RED}TOT${ChatColor.GRAY}] <${event.player.displayName}> ${event.message}")
            }

            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onTTTPlayerTrueDeath(event: TTTPlayerTrueDeathEvent) {
        if (event.winnerRoleGroup != RoleGroup.JACKAL && event.tttPlayer.role == Role.JACKAL) {
            val sidekicks = PlayerManager.tttPlayers.filter { it.role == Role.SIDEKICK }

            if (sidekicks.isNotEmpty()) {
                val newJackal = sidekicks.random()
                newJackal.changeRole(Role.JACKAL)

                event.tttPlayer.changeRole(Role.SIDEKICK) // The old Jackal

                sidekicks.forEach { sidekick ->
                    if (sidekick != newJackal) {
                        sidekick.player.sendMessage(TTTPlugin.prefix + "${newJackal.player.displayName} ${ChatColor.GREEN}ist der neue Jackal")
                    }
                }
            }
        }
    }

    val packetListener = object : PacketAdapter(plugin, PacketType.Play.Server.PLAYER_INFO) {
        override fun onPacketSending(event: PacketEvent) {
            val packet = WrapperPlayServerPlayerInfo(event.packet)

            if (
                packet.action == EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE ||
                packet.action == EnumWrappers.PlayerInfoAction.ADD_PLAYER
            ) {
                packet.data = packet.data.map { info ->
                    val tttPlayer = PlayerManager.tttPlayers.find { it.player.uniqueId == info.profile.uuid }

                    if (tttPlayer == null) info
                    else PlayerInfoData(
                        info.profile,
                        info.latency,
                        if (event.player.uniqueId == info.profile.uuid) {
                            if (event.player.gameMode == GameMode.SPECTATOR) EnumWrappers.NativeGameMode.SPECTATOR
                            else EnumWrappers.NativeGameMode.SURVIVAL
                        } else EnumWrappers.NativeGameMode.SURVIVAL,
                        info.displayName
                    )
                }.toMutableList()
            }
        }
    }
}
