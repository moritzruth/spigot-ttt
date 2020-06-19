package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.ScoreboardHelper
import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.classes.TTTClassCompanion
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.shop.Shop
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.properties.Delegates

class TTTPlayer(player: Player, role: Role, val tttClass: TTTClassCompanion = TTTClass.None) {
    var alive = true
    var player by Delegates.observable(player) { _, _, _ -> adjustPlayer() }

    val tttClassInstance = tttClass.createInstance(this)

    var role = role
        private set(value) {
            if (value !== field) {
                field = value
                scoreboard.updateRole()
                ScoreboardHelper.forEveryScoreboard { it.updateTeams() }
                scoreboard.showCorrectSidebarScoreboard()
                Shop.setItems(this)
            }
        }
    val roleHistory = mutableListOf<Role>()

    var walkSpeed
        get() = player.walkSpeed
        set(value) { player.walkSpeed = value }

    var credits = if (role.canOwnCredits) Settings.initialCredits else 0
        set(value) {
            field = value
            scoreboard.updateCredits()
        }

    val boughtItems = mutableListOf<TTTItem<*>>()

    val scoreboard = TTTScoreboard(this)

    init {
        adjustPlayer()
        scoreboard.initialize()
        tttClassInstance.tttPlayer = this
        tttClassInstance.init()
    }

    fun damage(damage: Double, reason: DeathReason, damager: TTTPlayer, scream: Boolean = true) {
        if (!alive) return

        val event = TTTPlayerDamageEvent(this, damage, reason).call()
        val finalHealth = player.health - event.damage

        if (finalHealth <= 0.0) onDeath(reason, damager, scream)
        else player.damage(damage)
    }

    fun onDeath(reason: DeathReason, killer: TTTPlayer?, scream: Boolean = true) {
        if (!alive) return

        alive = false
        player.gameMode = GameMode.SPECTATOR
        Shop.clear(this)

        var reallyScream = scream

        player.sendMessage(TTTPlugin.prefix +
                if (killer == null) "${ChatColor.RED}${ChatColor.BOLD}Du bist gestorben"
                else "${ChatColor.RED}${ChatColor.BOLD}Du wurdest von " +
                        "${ChatColor.RESET}${killer.player.displayName} " +
                        "${ChatColor.RESET}(${killer.role.coloredDisplayName}${ChatColor.RESET}) " +
                        "${ChatColor.RED}${ChatColor.BOLD}getötet"
        )

        if (GameManager.phase == GamePhase.PREPARING) {
            player.sendMessage("${TTTPlugin.prefix}${ChatColor.GRAY}${ChatColor.ITALIC}Du wirst nach der Vorbereitungsphase wiederbelebt")

            val event = TTTPlayerDeathInPreparingEvent(
                tttPlayer = this,
                location = player.location,
                killer = killer,
                scream = reallyScream
            ).call()

            reallyScream = event.scream
        } else {
            val tttCorpse = TTTCorpse.spawn(this, reason)
            credits = 0

            val onlyRemainingRoleGroup = PlayerManager.getOnlyRemainingRoleGroup()

            val event = TTTPlayerTrueDeathEvent(
                tttPlayer = this,
                location = player.location,
                tttCorpse = tttCorpse,
                killer = killer,
                scream = reallyScream,
                winnerRoleGroup = onlyRemainingRoleGroup
            ).call()

            reallyScream = event.scream
            if (GameManager.phase == GamePhase.COMBAT) {
                event.winnerRoleGroup?.run { GameManager.letRoleWin(primaryRole) }
            }
        }

        clearInventory(true)

        if (reallyScream) GameManager.world.playSound(
            player.location,
            Resourcepack.Sounds.playerDeath,
            SoundCategory.PLAYERS,
            1F,
            1F
        )
    }

    fun revive(location: Location, credits: Int = 0) {
        if (alive) throw AlreadyLivingException()

        alive = true
        this.credits = credits

        player.health = 20.0
        player.teleport(location)

        Shop.setItems(this)

        TTTPlayerReviveEvent(this).call()
        player.sendMessage(TTTPlugin.prefix + "${ChatColor.GREEN}${ChatColor.BOLD}Du wurdest wiederbelebt")

        nextTick { player.gameMode = GameMode.SURVIVAL }
    }

    class AlreadyLivingException: Exception("The player already lives")

    private fun adjustPlayer() {
        player.scoreboard = scoreboard.scoreboard
    }

    private fun getOwningTTTItemInstances() = player.inventory.hotbarContents
        .filterNotNull()
        .mapNotNull { ItemManager.getInstanceByItemStack(it) }

    fun changeRole(newRole: Role, notify: Boolean = true) {
        roleHistory.add(role)
        role = newRole

        if (notify) {
            val message = if (role == Role.SIDEKICK) {
                val jackal = PlayerManager.tttPlayers.find { it.role == Role.JACKAL }
                    ?: throw NoJackalLivingException()

                "${ChatColor.WHITE}Du bist jetzt ${role.coloredDisplayName} von ${jackal.role.chatColor}${jackal.player.displayName}"
            } else "${ChatColor.WHITE}Du bist jetzt ${role.coloredDisplayName}"

            player.sendTitle("", message, secondsToTicks(0.2), secondsToTicks(3), secondsToTicks(0.5))
        }
        PlayerManager.letRemainingRoleGroupWin()
    }

    class NoJackalLivingException: Exception("There is no living jackal for this sidekick")

    fun resetAfterGameEnd() {
        if (!alive) {
            player.teleportToWorldSpawn()
        }

        // Required to be delayed because of a Minecraft bug which sometimes turns players invisible
        nextTick { reset() }
    }

    fun reset() {
        if (player.isDead) {
            player.spigot().respawn()
        }

        player.gameMode = GameMode.SURVIVAL
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        player.health = 20.0
        player.walkSpeed = 0.2F // yes, this is the default value
        player.level = 0
        player.exp = 0F
        player.allowFlight = player.gameMode == GameMode.CREATIVE
        player.foodLevel = 20

        clearInventory(false)
        tttClassInstance.reset()
    }

    private fun clearInventory(becauseOfDeath: Boolean) {
        player.closeInventory()
        Shop.clear(this)
        val owningTTTItemInstances = getOwningTTTItemInstances()

        owningTTTItemInstances.forEach {
            if (!(tttClass.defaultItems.contains(it.tttItem) && GameManager.phase == GamePhase.PREPARING)) {
                removeItem(it.tttItem, becauseOfDeath = becauseOfDeath)
            }
        }
    }

    fun checkAddItemPreconditions(tttItem: TTTItem<*>) {
        val owningTTTItemInstances = getOwningTTTItemInstances()
        if (owningTTTItemInstances.find { it.tttItem === tttItem } != null) throw AlreadyHasItemException()

        val maxItemsOfTypeInInventory = tttItem.type.maxItemsOfTypeInInventory
        if (
            maxItemsOfTypeInInventory != null &&
            owningTTTItemInstances.filter { it.tttItem.type == tttItem.type }.count() >= maxItemsOfTypeInInventory
        ) throw TooManyItemsOfTypeException()
    }

    class AlreadyHasItemException: Exception("The player already owns this item")
    class TooManyItemsOfTypeException: Exception("The player already owns too much items of this type")

    fun addItem(item: TTTItem<*>) {
        checkAddItemPreconditions(item)
        val instance = item.createInstance()
        player.inventory.addItem(instance.createItemStack())
        instance.carrier = this
    }

    fun removeItem(item: TTTItem<*>, removeInstance: Boolean = true, becauseOfDeath: Boolean = false) {
        item.getInstance(this)?.let {
            it.carrier = null
            if (removeInstance && (!becauseOfDeath || it.tttItem.removeInstanceOnDeath)) {
                it.remove()
            }
        }

        player.inventory.removeTTTItem(item)
    }

    fun addDefaultClassItems() = tttClass.defaultItems.forEach { addItem(it) }

    override fun toString() = "TTTPlayer(${player.name} is $role)"

    companion object {
        fun of(player: Player) = PlayerManager.tttPlayers.find { it.player === player }
    }
}
