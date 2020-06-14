package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.Settings
import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.ScoreboardHelper
import de.moritzruth.spigot_ttt.game.classes.TTTClass
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.game.items.ItemManager
import de.moritzruth.spigot_ttt.game.items.Selectable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.impl.CloakingDevice
import de.moritzruth.spigot_ttt.game.items.shop.Shop
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.properties.Delegates

class TTTPlayer(player: Player, role: Role, val tttClass: TTTClass?) {
    var alive = true
    var player by Delegates.observable(player) { _, _, _ -> adjustPlayer() }

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

    var itemInHand by Delegates.observable<TTTItem?>(null) { _, oldItem, newItem ->
        if (oldItem !== newItem) onItemInHandChanged(oldItem, newItem)
    }

    var walkSpeed
        get() = player.walkSpeed
        set(value) { player.walkSpeed = value }

    var credits by Delegates.observable(Settings.initialCredits) { _, _, _ -> scoreboard.updateCredits() }
    val boughtItems = mutableListOf<TTTItem>()

    private var staminaCooldown: Int = 0
    private var staminaTask: BukkitTask? = null

    val scoreboard = TTTScoreboard(this)
    val stateContainer = StateContainer(this)

    init {
        adjustPlayer()
        scoreboard.initialize()
        tttClass?.onInit(this)
    }

    private fun onItemInHandChanged(oldItem: TTTItem?, newItem: TTTItem?) {
        if (oldItem !== null && oldItem is Selectable) {
            oldItem.onDeselect(this)
        }

        if (newItem !== null && newItem is Selectable) {
            newItem.onSelect(this)
        }
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

        var reallyScream = scream

        player.sendMessage(TTTPlugin.prefix +
                if (killer == null) "${ChatColor.RED}${ChatColor.BOLD}Du bist gestorben"
                else "${ChatColor.RED}${ChatColor.BOLD}Du wurdest von " +
                        "${ChatColor.RESET}${killer.player.displayName} " +
                        "${ChatColor.RESET}(${killer.role.coloredDisplayName}${ChatColor.RESET}) " +
                        " ${ChatColor.RED}${ChatColor.BOLD}getötet"
        )

        if (GameManager.phase == GamePhase.PREPARING) {
            player.sendMessage("${TTTPlugin.prefix}${ChatColor.GRAY}${ChatColor.ITALIC}Du wirst nach der Vorbereitungsphase wiederbelebt")

            val event = TTTPlayerDeathEvent(
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
            event.winnerRoleGroup?.run { GameManager.letRoleWin(primaryRole) }
        }

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

    private fun getOwningTTTItems() = player.inventory.hotbarContents.mapNotNull { it?.run { ItemManager.getItemByItemStack(this) } }

    fun activateStamina() {
        if (staminaTask != null) return

        staminaTask = plugin.server.scheduler.runTaskTimer(plugin, fun() {
            if (!alive) return

            if (player.isSprinting) {
                player.foodLevel -= 2
                staminaCooldown = 4
            } else {
                if (staminaCooldown == 0) {
                    if (player.foodLevel < 20) player.foodLevel += 2
                } else staminaCooldown -= 1
            }
        }, 0, secondsToTicks(0.5).toLong())
    }

    fun changeRole(newRole: Role) {
        roleHistory.add(role)
        role = newRole

        val message = if (role == Role.SIDEKICK) {
            val jackal = PlayerManager.tttPlayers.find { it.role == Role.JACKAL }
                ?: throw NoJackalLivingException()

            "${ChatColor.WHITE}Du bist jetzt ${role.coloredDisplayName} von ${jackal.role.chatColor}${jackal.player.displayName}"
        } else "${ChatColor.WHITE}Du bist jetzt ${role.coloredDisplayName}"

        player.sendTitle("", message, secondsToTicks(0.2), secondsToTicks(3), secondsToTicks(0.5))
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

        itemInHand?.let {
            if (it is Selectable) {
                it.onDeselect(this)
            }
        }

        player.gameMode = GameMode.SURVIVAL
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        player.health = 20.0
        player.walkSpeed = 0.2F // yes, this is the default value
        player.level = 0
        player.exp = 0F

        staminaTask?.cancel()
        staminaTask = null
        player.foodLevel = 20

        player.inventory.clear()
    }

    fun updateItemInHand() {
        val itemStack = player.inventory.itemInMainHand
        this.itemInHand =
                if (itemStack.type === Material.AIR) null
                else ItemManager.getItemByItemStack(itemStack)
    }

    fun checkAddItemPreconditions(item: TTTItem) {
        val owningTTTItems = getOwningTTTItems()

        if (owningTTTItems.contains(item)) {
            throw AlreadyHasItemException()
        }

        val maxItemsOfTypeInInventory = item.type.maxItemsOfTypeInInventory
        if (maxItemsOfTypeInInventory !== null && owningTTTItems.filter { it.type === item.type }.count() >= maxItemsOfTypeInInventory) {
            throw TooManyItemsOfTypeException()
        }
    }
    class AlreadyHasItemException: Exception("The player already owns this item")

    class TooManyItemsOfTypeException: Exception("The player already owns too much items of this type")

    fun addItem(item: TTTItem) {
        checkAddItemPreconditions(item)
        player.inventory.addItem(item.itemStack.clone())
        item.onOwn(this)
        updateItemInHand()
    }

    fun removeItem(item: TTTItem) {
        player.inventory.removeTTTItem(CloakingDevice)
        item.onRemove(this)
        updateItemInHand()
    }

    fun addDefaultClassItems() = tttClass?.defaultItems?.forEach { addItem(it) }

    override fun toString() = "TTTPlayer(${player.name} is $role)"

    companion object {
        fun of(player: Player) = PlayerManager.tttPlayers.find { it.player === player }
    }
}
