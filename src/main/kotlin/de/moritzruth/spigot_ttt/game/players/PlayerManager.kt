package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.JackalMode
import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.classes.TTTClassManager
import de.moritzruth.spigot_ttt.game.items.impl.SecondChance
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.nextTick
import de.moritzruth.spigot_ttt.utils.noop
import de.moritzruth.spigot_ttt.utils.teleportToWorldSpawn
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import kotlin.random.Random

object PlayerManager {
    val tttPlayers = mutableListOf<TTTPlayer>()

    private fun getAvailablePlayers() = plugin.server.onlinePlayers.filter { it.gameMode === GameMode.SURVIVAL }
    private fun getStillLivingRoleGroups() = getStillLivingRoles().map { it.group }.toSet()
    private fun getStillLivingRoles() = tttPlayers.filter {
        it.alive || SecondChance.getInstance(it)?.preventRoundEnd == true
    }.map { it.role }.toSet()

    private val playersJoinedDuringRound = mutableSetOf<Player>()

    fun getPlayersByRole() = mutableMapOf<Role, MutableSet<TTTPlayer>>()
            .apply { tttPlayers.forEach { getOrPut(it.role) { mutableSetOf() }.add(it) } }
            .toMap()

    fun resetAfterGame() {
        playersJoinedDuringRound.forEach {
            it.teleportToWorldSpawn()
            nextTick { it.gameMode = GameMode.SURVIVAL }
        }

        tttPlayers.forEach(TTTPlayer::resetAfterGameEnd)
        tttPlayers.clear()
    }

    fun letRemainingRoleGroupWin() {
        val onlyRemainingRoleGroup = getOnlyRemainingRoleGroup() ?: return
        GameManager.letRoleWin(onlyRemainingRoleGroup.primaryRole)
    }

    fun getOnlyRemainingRoleGroup(): RoleGroup? {
        GameManager.ensurePhase(GamePhase.COMBAT)
        return if (getStillLivingRoleGroups().count() == 1) getStillLivingRoleGroups().first()
        else null
    }

    fun onPlayerJoin(player: Player) {
        val tttPlayer = tttPlayers.find { it.player.uniqueId == player.uniqueId }

        if (tttPlayer == null) {
            if (GameManager.phase == null) {
                player.teleportToWorldSpawn()
                player.gameMode = GameMode.SURVIVAL
            } else {
                player.gameMode = GameMode.SPECTATOR
                playersJoinedDuringRound.add(player)
                player.sendMessage("${TTTPlugin.prefix}${ChatColor.GREEN}Du schaust jetzt zu.")
            }
        } else {
            tttPlayer.player = player

            player.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Du bist gestorben, da du das Spiel verlassen hast.")
            when(GameManager.phase) {
                GamePhase.PREPARING -> noop() // Never happens
                GamePhase.COMBAT -> {
                    player.gameMode = GameMode.SPECTATOR
                }
                GamePhase.OVER -> {
                    tttPlayer.player.teleportToWorldSpawn()
                    player.gameMode = GameMode.SURVIVAL
                }
            }
        }
    }

    fun onPlayerQuit(player: Player) {
        playersJoinedDuringRound.remove(player)
        val tttPlayer = TTTPlayer.of(player) ?: return

        when(GameManager.phase) {
            GamePhase.PREPARING -> {
                GameManager.abortGame()
                GameMessenger.abortedPlayerLeft()
            }
            GamePhase.COMBAT -> tttPlayer.onDeath(DeathReason.DISCONNECTED, null)
            GamePhase.OVER -> noop()
        }
    }

    fun createTTTPlayers() {
        val playersWithoutRole = getAvailablePlayers().toMutableSet()
        val playerCount = playersWithoutRole.count()

        if (Settings.traitorCount < 1) throw IllegalStateException("roles.traitor.count may not be lower than 1")

        var requiredPlayerCount = Settings.traitorCount
        if (Settings.detectiveEnabled) requiredPlayerCount += 1
        if (Settings.jackalMode != JackalMode.NEVER) requiredPlayerCount += 1
        requiredPlayerCount += 1 // Innocent
        if (playerCount < requiredPlayerCount) throw NotEnoughPlayersException(playerCount, requiredPlayerCount)

        val classesIterator = TTTClassManager.createClassesIterator(playerCount)

        fun createTTTPlayer(role: Role) {
            val player = playersWithoutRole.random()
            tttPlayers.add(TTTPlayer(player, role, classesIterator.next()))
            playersWithoutRole.remove(player)
        }

        for (index in 1..Settings.traitorCount) createTTTPlayer(Role.TRAITOR)

        if (Settings.detectiveEnabled) createTTTPlayer(Role.DETECTIVE)

        if (when (Settings.jackalMode) {
                JackalMode.ALWAYS -> true
                JackalMode.HALF_TIME -> Random.Default.nextBoolean()
                JackalMode.NEVER -> false
            }
        ) createTTTPlayer(Role.JACKAL)

        playersWithoutRole.forEach { tttPlayers.add(TTTPlayer(it, Role.INNOCENT, classesIterator.next())) }
    }

    class NotEnoughPlayersException(
        val actual: Int,
        val required: Int
    ): Exception("There are not enough players to start the game")
}
