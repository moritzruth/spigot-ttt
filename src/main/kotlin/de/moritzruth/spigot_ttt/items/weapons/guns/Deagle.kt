package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.DETECTIVE
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor
import java.util.*

object Deagle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.BLUE}${ChatColor.BOLD}Deagle",
    damage = heartsToHealth(5.0),
    cooldown = 1.4,
    magazineSize = 8,
    reloadTime = 3.0,
    itemMaterial = CustomItems.deagle,
    recoil = 10
), Buyable, Spawning {
    override val buyableBy: EnumSet<TTTPlayer.Role> = EnumSet.of(TRAITOR, DETECTIVE)
    override val price = 1
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


