package de.moritzruth.spigot_ttt.game.players

import java.util.*

enum class RoleGroup(val primaryRole: Role, val additionalRoles: EnumSet<Role> = EnumSet.noneOf(Role::class.java)) {
    INNOCENT(Role.INNOCENT, EnumSet.of(Role.DETECTIVE)),
    JACKAL(Role.JACKAL, EnumSet.of(Role.SIDEKICK)),
    TRAITOR(Role.TRAITOR);

    fun bothAre(firstTTTPlayer: TTTPlayer, secondTTTPlayer: TTTPlayer) =
        firstTTTPlayer.role.group == this && secondTTTPlayer.role.group == this

    companion object {
        fun getGroupOf(role: Role) = values().find { it.primaryRole == role || it.additionalRoles.contains(role) }
    }
}
