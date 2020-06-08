package de.moritzruth.spigot_ttt.items.impl

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.isRightClick
import de.moritzruth.spigot_ttt.utils.removeTTTItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object FakeCorpse: TTTItem, Buyable {
    private val DISPLAY_NAME = "${ChatColor.YELLOW}${ChatColor.BOLD}Fake-Leiche"

    override val itemStack = ItemStack(ResourcePack.Items.fakeCorpse).applyMeta {
        setDisplayName(DISPLAY_NAME)
        lore = listOf(
                "",
                "${ChatColor.GOLD}Spawnt eine Fake-Leiche",
                "${ChatColor.GOLD}Rolle und Spieler auswählbar"
        )
        hideInfo()
    }
    override val type = TTTItem.Type.SPECIAL
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 2
    override val buyLimit: Int? = 3

    val isc = InversedStateContainer(State::class)

    private val chooseRoleInventory = plugin.server.createInventory(
        null,
        InventoryType.HOPPER,
        "${DISPLAY_NAME}${ChatColor.RESET} - Rolle"
    ).apply {
        addItem(*Role.values()
            .map {
                ItemStack(it.iconItemMaterial).applyMeta {
                    setDisplayName(it.coloredDisplayName)
                    hideInfo()
                }
            }
            .toTypedArray())
    }

    private fun createChoosePlayerInventory() = plugin.server.createInventory(
        null,
        InventoryType.CHEST,
        "${DISPLAY_NAME}${ChatColor.RESET} - Spieler"
    ).apply {
        addItem(*PlayerManager.tttPlayers
            .map {
                ItemStack(Material.PLAYER_HEAD).applyMeta {
                    setDisplayName("${ChatColor.RESET}${it.player.displayName}")
                    hideInfo()
                }.apply {
                    itemMeta = (itemMeta!! as SkullMeta).apply { owningPlayer = it.player }
                }
            }
            .toTypedArray())
    }

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(FakeCorpse)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(FakeCorpse)) return
            event.isCancelled = true

            if (event.action.isRightClick) {
                val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
                isc.getOrCreate(tttPlayer).chosenRole = null
                tttPlayer.player.openInventory(chooseRoleInventory)
            }
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) {
            if (event.whoClicked !is Player) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.whoClicked as Player) ?: return
            val state = isc.getOrCreate(tttPlayer)

            if (!(
                event.clickedInventory == chooseRoleInventory ||
                event.clickedInventory == state.choosePlayerInventory
            )) return
            event.isCancelled = true

            val item = event.currentItem

            if (item != null && event.click == ClickType.LEFT) {
                when (event.clickedInventory) {
                    chooseRoleInventory -> {
                        state.chosenRole = Role.values()[event.slot]
                        state.choosePlayerInventory = createChoosePlayerInventory()
                        tttPlayer.player.openInventory(state.choosePlayerInventory!!)
                    }
                    state.choosePlayerInventory -> {
                        tttPlayer.player.closeInventory()

                        val corpsePlayer = plugin.server.getPlayer((item.itemMeta as SkullMeta).owningPlayer!!.uniqueId)!!
                        val corpseTTTPlayer = PlayerManager.getTTTPlayer(corpsePlayer)

                        if (corpseTTTPlayer == null) {
                            ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Das hat nicht funktioniert")
                        } else {
                            TTTCorpse.spawnFake(state.chosenRole!!, corpseTTTPlayer, tttPlayer.player.location)

                            tttPlayer.player.inventory.removeTTTItem(FakeCorpse)
                        }
                    }
                }
            }
        }
    }

    class State: IState {
        var chosenRole: Role? = null
        var choosePlayerInventory: Inventory? = null
    }
}
