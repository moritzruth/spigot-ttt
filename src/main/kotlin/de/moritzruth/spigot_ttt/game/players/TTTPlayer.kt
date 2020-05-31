package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.discord.DiscordBot
import de.moritzruth.spigot_ttt.discord.DiscordInterface
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.players.corpses.CorpseManager
import de.moritzruth.spigot_ttt.items.ItemManager
import de.moritzruth.spigot_ttt.items.SelectableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.shop.Shop
import de.moritzruth.spigot_ttt.utils.hotbarContents
import de.moritzruth.spigot_ttt.utils.teleportPlayerToWorldSpawn
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.properties.Delegates

class TTTPlayer(player: Player, role: Role) {
    var alive = true
    var lastDeathReason: DeathReason? = null

    var player by Delegates.observable(player) { _, _, player -> player.scoreboard = scoreboard.scoreboard }

    var role by Delegates.observable(role) { _, _, _ -> scoreboard.updateRole() }
    val roleHistory = mutableListOf<Role>()

    var itemInHand by Delegates.observable<TTTItem?>(null) { _, oldItem, newItem -> onItemInHandChanged(oldItem, newItem) }
    var credits by Delegates.observable(10) { _, _, _ -> scoreboard.updateCredits() }

    val scoreboard = TTTScoreboard(this)
    val stateContainer = StateContainer()

    private val discordUser get() = DiscordInterface.getUserByPlayerUUID(player.uniqueId)

    init {
        player.scoreboard = scoreboard.scoreboard
        scoreboard.initialize()
    }

    private fun onItemInHandChanged(oldItem: TTTItem?, newItem: TTTItem?) {
        if (oldItem !== null && oldItem is SelectableItem) {
            oldItem.onDeselect(this)
        }

        if (newItem !== null && newItem is SelectableItem) {
            newItem.onSelect(this)
        }
    }

    fun kill(reason: DeathReason = DeathReason.SUICIDE) {
        GameManager.ensurePhase(GamePhase.COMBAT)

        player.gameMode = GameMode.SPECTATOR
        alive = false
        lastDeathReason = reason
        CorpseManager.spawn(this, reason)

        Shop.hide(this)
        setMuted(true)

        PlayerManager.letRemainingRoleWin()
    }

    fun resetAfterGameEnd() {
        if (!alive) {
            teleportToSpawn()
        }

        // Required to be delayed because of a Minecraft bug which sometimes turns players invisible
        plugin.server.scheduler.runTask(plugin) { ->
            reset()
        }
    }

    fun reset() {
        if (player.isDead) {
            player.spigot().respawn()
        }

        itemInHand?.apply {
            if (this is SelectableItem) {
                this.onDeselect(this@TTTPlayer)
            }
        }

        stateContainer.clear()

        setMuted(false)

        alive = true
        player.gameMode = GameMode.SURVIVAL
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        player.health = 20.0

        player.inventory.clear()
    }

    fun teleportToSpawn() {
        teleportPlayerToWorldSpawn(player)
    }

    fun setMuted(muted: Boolean) {
        val discordUser = discordUser

        if (discordUser != null) {
            DiscordBot.setMuted(discordUser, muted)
        }
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

    fun addItem(item: TTTItem) {
        checkAddItemPreconditions(item)
        player.inventory.addItem(item.itemStack.clone())
    }

    class AlreadyHasItemException: Exception("The player already owns this item")
    class TooManyItemsOfTypeException: Exception("The player already owns too much items of this type")

    fun getOwningTTTItems() = player.inventory.hotbarContents.mapNotNull { it?.run { ItemManager.getItemByItemStack(this) } }

    enum class Role(
        val chatColor: ChatColor,
        val displayName: String,
        val iconItemMaterial: Material
    ) {
        INNOCENT(ChatColor.GREEN, "Innocent", CustomItems.innocent),
        DETECTIVE(ChatColor.YELLOW, "Detective", CustomItems.detective),
        TRAITOR(ChatColor.RED, "Traitor", CustomItems.traitor),
        JACKAL(ChatColor.AQUA, "Jackal", CustomItems.jackal),
        SIDEKICK(ChatColor.AQUA, "Sidekick", CustomItems.sidekick);

        val coloredDisplayName = "$chatColor$displayName${ChatColor.RESET}"

        val isInnocentRole get() = this === INNOCENT || this === DETECTIVE
        val isJackalRole get() = this === JACKAL || this === SIDEKICK

        val position by lazy { values().indexOf(this) }
    }
}
