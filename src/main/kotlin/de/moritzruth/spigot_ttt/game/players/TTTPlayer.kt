package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.shop.Shop
import de.moritzruth.spigot_ttt.utils.hotbarContents
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.teleportPlayerToWorldSpawn
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import kotlin.properties.Delegates

class TTTPlayer(player: Player, role: Role) {
    var alive = true
    var player by Delegates.observable(player) { _, _, _ -> adjustPlayer() }

    var role = role
        private set(value) {
            if (value !== field) {
                field = value
                scoreboard.updateRole()
                scoreboard.showCorrectSidebarScoreboard()
                Shop.setItems(this)
            }
        }
    val roleHistory = mutableListOf<Role>()

    var itemInHand by Delegates.observable<TTTItem?>(null) { _, oldItem, newItem ->
        if (oldItem !== newItem) onItemInHandChanged(oldItem, newItem)
    }
    var credits by Delegates.observable(10) { _, _, _ -> scoreboard.updateCredits() }
    val boughtItems = mutableListOf<TTTItem>()

    var invisible by Delegates.observable(false) { _, _, value ->
        if (value) {
            PlayerManager.tttPlayers.forEach {
                if (it.alive && it.role != role) {
                    it.player.hidePlayer(plugin, player)
                }
            }

            // for the translucent effect seen by teammates
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false))
        } else {
            plugin.server.onlinePlayers.forEach { it.showPlayer(plugin, player) }
            player.removePotionEffect(PotionEffectType.INVISIBILITY)
        }
    }
    var damageInfo: DamageInfo? = null

    private var staminaCooldown: Int = 0
    private var staminaTask: BukkitTask? = null

    val scoreboard = TTTScoreboard(this)
    val stateContainer = StateContainer(this)

    init {
        adjustPlayer()
        scoreboard.initialize()
    }

    private fun onItemInHandChanged(oldItem: TTTItem?, newItem: TTTItem?) {
        if (oldItem !== null && oldItem is Selectable) {
            oldItem.onDeselect(this)
        }

        if (newItem !== null && newItem is Selectable) {
            newItem.onSelect(this)
        }
    }

    fun onDeath(reason: DeathReason, killer: TTTPlayer?) {
        if (killer == this) throw IllegalArgumentException("You cannot be your own killer")
        GameManager.ensurePhase(GamePhase.COMBAT)

        player.sendMessage(TTTPlugin.prefix +
            if (killer == null) "${ChatColor.RED}${ChatColor.BOLD}Du bist gestorben"
            else "${ChatColor.RED}${ChatColor.BOLD}Du wurdest von " +
                    "${ChatColor.RESET}${killer.player.displayName}" +
                    "${ChatColor.RED}${ChatColor.BOLD} getötet"
        )

        player.gameMode = GameMode.SPECTATOR
        alive = false
        val tttCorpse = TTTCorpse.spawn(this, reason)

        player.inventory.clear()
        credits = 0

        val event = TTTPlayerDeathEvent(this, player.location, tttCorpse, killer)
        plugin.server.pluginManager.callEvent(event)

        if (event.letRoundEnd) {
            PlayerManager.letRemainingRoleGroupWin()
        }
    }

    fun revive(location: Location, credits: Int = 0) {
        if (alive) throw AlreadyLivingException()

        alive = true
        this.credits = credits

        player.health = 20.0
        player.teleport(location)

        Shop.setItems(this)

        plugin.server.pluginManager.callEvent(TTTPlayerReviveEvent(this))
        player.sendMessage(TTTPlugin.prefix + "${ChatColor.GREEN}${ChatColor.BOLD}Du wurdest wiederbelebt")

        plugin.server.scheduler.runTask(plugin, fun() {
            player.gameMode = GameMode.SURVIVAL
        })
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
            teleportToSpawn()
        }

        // Required to be delayed because of a Minecraft bug which sometimes turns players invisible
        plugin.server.scheduler.runTask(plugin, fun() {
            reset()
        })
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

        invisible = false

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

    fun teleportToSpawn() = teleportPlayerToWorldSpawn(player)

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
        updateItemInHand()
    }

    override fun toString() = "TTTPlayer(${player.name} is $role)"
}
