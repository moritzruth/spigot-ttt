package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.corpses.CorpseListener
import de.moritzruth.spigot_ttt.game.corpses.CorpseManager
import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.items.ItemSpawner
import de.moritzruth.spigot_ttt.game.items.shop.Shop
import de.moritzruth.spigot_ttt.game.items.shop.ShopListener
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.call
import org.bukkit.GameRule
import kotlin.random.Random

object GameManager {
    var phase: GamePhase? = null
        private set(value) {
            field = value
            ScoreboardHelper.forEveryScoreboard { it.updateEverything(); it.showCorrectSidebarScoreboard() }
        }

    val world = plugin.server.getWorld("world")!!

    fun initialize() {
        adjustWorld()

        ItemManager.registerListeners()
        GameListener.register()
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
            Shop.clear(it)
        }

        Timers.startOverPhaseTimer(plugin.config.getInt("duration.over", 10)) {
            GameEndEvent(false).call()

            phase = null
            resetWorld()
            PlayerManager.resetAfterGame()
        }

        GameMessenger.roles()
    }

    private fun adjustWorld() {
        world.apply {
            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false)
            setGameRule(GameRule.DISABLE_RAIDS, true)
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_ENTITY_DROPS, false)
            setGameRule(GameRule.DO_FIRE_TICK, false)
            setGameRule(GameRule.LOG_ADMIN_COMMANDS, false)
            setGameRule(GameRule.DO_MOB_LOOT, false)
            setGameRule(GameRule.KEEP_INVENTORY, true) // will be cleared
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.NATURAL_REGENERATION, false)
            setGameRule(GameRule.MOB_GRIEFING, false)
            setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
        }
    }

    fun resetWorld() {
        CorpseManager.destroyAll()
        ItemManager.reset()

        world.run {
            setStorm(false)
            time = 0
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        }
    }

    fun abortGame(broadcast: Boolean = false) {
        if (phase === null) throw IllegalStateException("The game is not running")

        GameEndEvent(true).call()
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

        world.run {
            setStorm(Random.nextInt(4) == 1)
            time = Random.nextLong(0, 23999)
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            setGameRule(GameRule.DO_WEATHER_CYCLE, true)
        }

        phase = GamePhase.PREPARING
        PlayerManager.createTTTPlayers()

        PlayerManager.tttPlayers.forEach {
            it.reset()
            it.teleportToSpawn()
            it.activateStamina()
        }

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
        PlayerManager.tttPlayers.forEach { Shop.setItems(it) }
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
