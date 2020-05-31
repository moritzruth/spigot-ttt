package de.moritzruth.spigot_ttt.discord

import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.randomNumber
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import de.moritzruth.spigot_ttt.utils.toTrimmedString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitTask
import java.awt.Color
import java.util.*

object DiscordBot: ListenerAdapter() {
    private val MC_PREFIX = "${ChatColor.GRAY}[${ChatColor.AQUA}Discord${ChatColor.GRAY}]${ChatColor.RESET} "

    private val validations = mutableSetOf<ValidationData>()

    lateinit var jda: JDA
    private val guild get() = jda.guilds[0]!!

    fun start() {
        jda = JDABuilder.createDefault(plugin.config.getString("discord-token")).build()

        jda.presence.setStatus(OnlineStatus.ONLINE)
        jda.presence.activity = Activity.of(Activity.ActivityType.CUSTOM_STATUS, "Schick mir eine PN mit deinem Minecraft-Namen")

        jda.addEventListener(this)
    }

    fun stop() {
        jda.shutdownNow()
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (event.author == jda.selfUser) return

        val data = validations.find { it.userID == event.author.id }
        if (data == null) {
            val player = plugin.server.getPlayerExact(event.message.contentRaw)

            if (player == null) {
                event.message.privateChannel.sendMessage(EmbedBuilder()
                    .setTitle("Dieser Spieler existiert nicht oder ist nicht online.")
                    .setColor(Color.RED)
                    .build()
                ).queue()
            } else {
                val existingConnectionOfPlayer = DiscordConnections.getByPlayerUUID(player.uniqueId)
                val avatarURL = "https://crafatar.com/avatars/${player.uniqueId.toTrimmedString()}"

                if (existingConnectionOfPlayer != null) {
                    if (existingConnectionOfPlayer.userID == event.author.id) {
                        event.message.privateChannel.sendMessage(EmbedBuilder()
                            .setTitle("Du hast diesen Account bereits verknüpft.")
                            .setColor(Color.GREEN)
                            .setThumbnail(avatarURL)
                            .build()
                        ).queue()
                    } else {
                        event.message.privateChannel.sendMessage(EmbedBuilder()
                            .setTitle("Dieser Spieler hat bereits einen Account verknüpft.")
                            .setDescription("Du kannst die Verknüpfung widerrufen, indem du `/discord revoke` in den Chat schreibst.")
                            .setColor(Color.decode("#ffe414"))
                            .setThumbnail(avatarURL)
                            .build()
                        )
                    }

                    return
                }

                println("https://crafatar.com/avatars/${player.uniqueId.toTrimmedString()}")

                val embedBuilder = EmbedBuilder()
                    .setTitle("Perfekt! Jetzt schick mir den Bestätigungscode, den du soeben in Minecraft erhalten hast.")
                    .setColor(Color.decode("#0095ff"))
                    .setThumbnail(avatarURL)

                if (DiscordConnections.getByUserID(event.author.id) != null) {
                    embedBuilder.setDescription("Hinweis: Du hast bereits einen Account verknüpft. Diese Verknüpfung wird gelöscht, wenn du einen neuen Account verbindest.")
                }

                val nextStepMessagePromise = event.message.privateChannel.sendMessage(
                    embedBuilder.build()
                ).submit()

                val newData = ValidationData(player.uniqueId, event.author.id, randomNumber(1000, 9999).toShort())

                newData.timeoutTask = plugin.server.scheduler.runTaskLater(plugin, fun() {
                    validations.remove(newData)

                    player.sendMessage("${MC_PREFIX}${ChatColor.RED}Dein Bestätigungscode ist abgelaufen.")
                    nextStepMessagePromise.getNow(null)?.editMessage(EmbedBuilder()
                        .setTitle("Abgelaufen.")
                        .setColor(Color.LIGHT_GRAY)
                        .build())?.queue()
                }, secondsToTicks(30).toLong())

                validations.add(newData)

                player.sendMessage("${MC_PREFIX}Dein Bestätigungscode (läuft in 30 Sekunden ab): ${ChatColor.GOLD}${ChatColor.BOLD}${newData.code}")
            }
        } else {
            if (data.code.toString() == event.message.contentRaw) {
                validations.remove(data)
                data.timeoutTask.cancel()

                event.message.privateChannel.sendMessage(EmbedBuilder()
                    .setTitle("Dein Minecraft-Account ist jetzt verknüpft.")
                    .setColor(Color.GREEN)
                    .build()
                ).queue()

                plugin.server.getPlayer(data.playerUUID)?.sendMessage(
                    "${MC_PREFIX}Dein Minecraft-Account ist jetzt mit dem Discord-Account " +
                            "${ChatColor.GOLD}${event.author.name}#${event.author.discriminator} " +
                            "${ChatColor.RESET}verknüpft.")

                DiscordConnections.add(DiscordConnection(data.playerUUID, data.userID))
            } else {
                event.message.privateChannel.sendMessage(EmbedBuilder()
                    .setTitle("Der Code ist leider falsch.")
                    .setColor(Color.RED)
                    .build()
                ).queue()
            }
        }
    }

    fun setMuted(user: User, muted: Boolean) {
        guild.getMember(user)?.mute(muted)?.queue()
    }

    data class ValidationData(
        val playerUUID: UUID,
        val userID: String,
        val code: Short
    ) {
        lateinit var timeoutTask: BukkitTask
    }
}
