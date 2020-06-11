package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

object SidekickDeagle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.AQUA}${ChatColor.BOLD}Sidekick Deagle",
    damage = 0.1, // Not really
    cooldown = 1.0,
    magazineSize = 1,
    reloadTime = 0.0,
    itemMaterial = Resourcepack.Items.sidekickDeagle,
    shootSound = Resourcepack.Sounds.Item.Weapon.Deagle.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Deagle.reload
), Buyable {
    override val buyableBy = roles(Role.JACKAL)
    override val price = 1
    override val type = TTTItem.Type.PISTOL_LIKE
    override val buyLimit = 1

    override val itemStack = ItemStack(Resourcepack.Items.sidekickDeagle).applyMeta {
        hideInfo()
        setDisplayName("${ChatColor.AQUA}${ChatColor.BOLD}Sidekick Deagle")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Mache einen Spieler zu deinem Sidekick",
            "",
            "${ChatColor.RED}Nur ein Schuss"
        )
    }

    override fun reload(tttPlayer: TTTPlayer, itemStack: ItemStack, state: Gun.State) {
        ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Du kannst diese Waffe nicht nachladen")
    }

    override fun onHit(tttPlayer: TTTPlayer, hitTTTPlayer: TTTPlayer) {
        hitTTTPlayer.changeRole(Role.SIDEKICK)
    }

    override fun onDrop(tttPlayer: TTTPlayer, itemEntity: Item): Boolean {
        val state = isc.get(tttPlayer) ?: return true

        return if (tttPlayer.role != Role.JACKAL || state.remainingShots == 0) {
            isc.remove(tttPlayer)
            itemEntity.remove()
            state.currentAction?.task?.cancel()
            true
        } else false
    }

    override fun onBeforeShoot(tttPlayer: TTTPlayer, item: ItemStack, state: Gun.State): Boolean {
        if (tttPlayer.role != Role.JACKAL) {
            ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}Diese Waffe kann nur der Jackal benutzen")
            return false
        }

        return super.onBeforeShoot(tttPlayer, item, state)
    }

    class State: Gun.State(magazineSize)
}


