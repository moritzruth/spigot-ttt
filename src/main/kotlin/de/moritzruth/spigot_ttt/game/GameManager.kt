package de.moritzruth.spigot_ttt.game

import com.comphenix.protocol.ProtocolLibrary
import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.game.classes.TTTClassManager
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
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import de.moritzruth.spigot_ttt.utils.teleportToWorldSpawn
import org.bukkit.*
import org.bukkit.block.Block
import kotlin.random.Random

object GameManager {
    var phase: GamePhase? = null
        private set(value) {
            field = value
            ScoreboardHelper.forEveryScoreboard { it.updateEverything(); it.showCorrectSidebarScoreboard() }
        }

    val world = plugin.server.getWorld("world")!!

    val destroyedBlocks = mutableMapOf<Location, Material>()

    private val listeners = ItemManager.listeners
        .plus(GameListener)
        .plus(ShopListener)
        .plus(CorpseListener)
        .plus(TTTClassManager.listeners)

    private val packetListeners = ItemManager.packetListeners
        .plus(GameListener.packetListener)

    fun initialize() {
        adjustWorld()

        listeners.forEach { plugin.server.pluginManager.registerEvents(it, plugin) }
        packetListeners.forEach { ProtocolLibrary.getProtocolManager().addPacketListener(it) }
    }

    fun letRoleWin(role: Role?) {
        ensurePhase(GamePhase.COMBAT)
        GameMessenger.win(role)
        phase = GamePhase.OVER
        Timers.cancelCurrentTask()
        ScoreboardHelper.forEveryScoreboard { it.updateTeams() }

        PlayerManager.tttPlayers.forEach {
            Shop.clear(it)
        }

        Timers.startOverPhaseTimer {
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

        destroyedBlocks.forEach { (location, material) ->
            world.getBlockAt(location).type = material
        }
        destroyedBlocks.clear()

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

        if (broadcast) {
            GameMessenger.aborted()
        }
    }

    fun startPreparingPhase() {
        ensurePhase(null)

        PlayerManager.createTTTPlayers()
        phase = GamePhase.PREPARING

        world.run {
            setStorm(Random.nextInt(4) == 1)
            time = Random.nextLong(0, 23999)
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            setGameRule(GameRule.DO_WEATHER_CYCLE, true)
        }

        PlayerManager.tttPlayers.forEach {
            it.player.teleportToWorldSpawn()
            it.addDefaultClassItems()
            it.player.health = heartsToHealth(10.0)
        }

        GameMessenger.preparingPhaseStarted()
        Timers.playTimerSound()
        ItemSpawner.spawnWeapons()

        Timers.startPreparingPhaseTimer {
            startCombatPhase()
        }
    }

    private fun startCombatPhase() {
        ensurePhase(GamePhase.PREPARING)
        phase = GamePhase.COMBAT

        PlayerManager.tttPlayers.forEach {
            Shop.setItems(it)

            if (!it.alive) {
                it.revive(world.spawnLocation, Settings.initialCredits)
            }
        }
        ScoreboardHelper.forEveryScoreboard { it.updateTeams() }

        Timers.playTimerSound()
        GameMessenger.combatPhaseStarted()

        Timers.startCombatPhaseTimer {
            if (PlayerManager.stillLivingRoles.contains(Role.INNOCENT)) {
                letRoleWin(Role.INNOCENT)
            } else {
                letRoleWin(null)
            }
        }
    }

    fun destroyBlock(block: Block) {
        if (phase != null && block.type.toString().contains("glass_pane", true)) {
            destroyedBlocks[block.location] = block.type
            block.type = Material.AIR
            world.playSound(
                block.location,
                Sound.BLOCK_GLASS_BREAK,
                SoundCategory.BLOCKS,
                1F,
                1F
            )
        }
    }

    fun ensurePhase(phase: GamePhase?) {
        if (this.phase !== phase) throw IllegalStateException("The game must be in $phase phase")
    }
}
