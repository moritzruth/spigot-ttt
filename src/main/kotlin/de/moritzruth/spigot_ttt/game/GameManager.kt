package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.corpses.CorpseListener
import de.moritzruth.spigot_ttt.game.corpses.CorpseManager
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.items.ItemSpawner
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.shop.Shop
import de.moritzruth.spigot_ttt.shop.ShopListener

object GameManager {
    var phase: GamePhase? = null
        private set(value) {
            field = value
            ScoreboardHelper.forEveryScoreboard { it.updateEverything(); it.showCorrectSidebarScoreboard() }
        }

    val world = plugin.server.getWorld("world")!!

    fun initialize() {
        ItemManager.registerListeners()
        GeneralGameEventsListener.register()
        ShopListener.register()
        CorpseListener.register()
    }

    fun letRoleWin(role: Role?) {
        ensurePhase(GamePhase.COMBAT)
        GameMessenger.win(role)
        phase = GamePhase.OVER
        Timers.cancelCurrentTask()
        Shop.stopCreditsTimer()
        ScoreboardHelper.forEveryScoreboard { it.updateTeams() }

        PlayerManager.tttPlayers.forEach {
            it.setMuted(false)
            Shop.hide(it)
        }

        Timers.startOverPhaseTimer(plugin.config.getInt("duration.over", 10)) {
            phase = null
            resetWorld()

            PlayerManager.resetAfterGame()
        }

        GameMessenger.roles()
    }

    fun resetWorld() {
        CorpseManager.destroyAll()
        ItemManager.reset()
    }

    fun abortGame(broadcast: Boolean = false) {
        if (phase === null) throw IllegalStateException("The game is not running")

        phase = null
        Timers.cancelCurrentTask()
        resetWorld()
        PlayerManager.resetAfterGame()
        Shop.stopCreditsTimer()

        if (broadcast) {
            GameMessenger.aborted()
        }
    }

    fun startPreparingPhase() {
        ensurePhase(null)

        if (PlayerManager.availablePlayers.count() < plugin.config.getInt("min-players", 4)) {
            throw NotEnoughPlayersException()
        }

        PlayerManager.createTTTPlayers()
        phase = GamePhase.PREPARING

        PlayerManager.tttPlayers.forEach { it.reset(); it.teleportToSpawn() }
        GameMessenger.preparingPhaseStarted()
        Timers.playTimerSound()
        ItemSpawner.spawnWeapons()

        Timers.startPreparingPhaseTimer(plugin.config.getInt("duration.preparing", 20)) {
            startCombatPhase()
        }
    }

    private fun startCombatPhase() {
        ensurePhase(GamePhase.PREPARING)

        phase = GamePhase.COMBAT
        PlayerManager.tttPlayers.forEach { Shop.show(it) }
        ScoreboardHelper.forEveryScoreboard { it.updateTeams() }
        Shop.startCreditsTimer()

        Timers.playTimerSound()
        GameMessenger.combatPhaseStarted()

        Timers.startCombatPhaseTimer(plugin.config.getInt("duration.combat", 480)) {
            if (PlayerManager.stillLivingRoles.contains(Role.INNOCENT)) {
                letRoleWin(Role.INNOCENT)
            } else {
                letRoleWin(null)
            }
        }
    }

    fun ensurePhase(phase: GamePhase?) {
        if (this.phase !== phase) throw IllegalStateException("The game must be in $phase phase")
    }

    class NotEnoughPlayersException: Exception("There are not enough players to start the game")
}
