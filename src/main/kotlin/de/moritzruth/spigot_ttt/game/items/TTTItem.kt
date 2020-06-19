package de.moritzruth.spigot_ttt.game.items

import com.comphenix.protocol.events.PacketListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.Probability
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.nextTick
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class TTTItem<InstanceT: TTTItem.Instance>(
    val type: Type,
    val templateItemStack: ItemStack,
    val instanceType: KClass<out InstanceT>,
    val shopInfo: ShopInfo? = null,
    val spawnProbability: Probability? = null,
    val disableDamage: Boolean = true
) {
    open val listener: Listener? = null
    open val packetListener: PacketListener? = null

    val material = templateItemStack.type

    val instancesByUUID = mutableMapOf<UUID, InstanceT>()
    fun getInstance(itemStack: ItemStack) =
        itemStack.itemMeta?.persistentDataContainer?.get(ID_KEY, PersistentDataType.STRING)
            ?.let { instancesByUUID[UUID.fromString(it)] }

    fun getInstance(tttPlayer: TTTPlayer) = instancesByUUID.values.find { it.carrier === tttPlayer }

    fun reset() {
        instancesByUUID.values.forEach {
            it.carrier?.removeItem(it.tttItem, removeInstance = false)
            it.reset()
        }
        instancesByUUID.clear()
    }

    fun createInstance(): InstanceT = instanceType.primaryConstructor!!.call()
        .also { instancesByUUID[it.uuid] = it }

    enum class Type(val maxItemsOfTypeInInventory: Int?) {
        MELEE(1),
        PISTOL_LIKE(2),
        HEAVY_WEAPON(1),
        SPECIAL(null);
    }

    data class ShopInfo(
        val buyableBy: EnumSet<Role>,
        val price: Int,
        val buyLimit: Int = 0
    ) {
        init {
            if (buyLimit < 0) throw IllegalArgumentException("buyLimit must be positive")
        }
    }

    companion object {
        val PASSIVE_SUFFIX = " ${ChatColor.RESET}${ChatColor.RED}(Passiv)"
        val ID_KEY = NamespacedKey(plugin, "instance")
    }

    abstract class Instance(val tttItem: TTTItem<*>, droppable: Boolean = true) {
        val uuid = UUID.randomUUID()!!

        fun createItemStack() = tttItem.templateItemStack.clone().applyMeta {
            persistentDataContainer.set(ID_KEY, PersistentDataType.STRING, uuid.toString())
        }

        private var isFirstCarrier = true
        open var carrier: TTTPlayer? = null
            set(newCarrier) {
                if (newCarrier == field) return // Do nothing it does not get changed

                if (newCarrier == null) {
                    val oldCarrier = field!!
                    isSelected = false
                    field = newCarrier
                    onCarrierRemoved(oldCarrier)
                } else {
                    field = newCarrier
                    onCarrierSet(newCarrier, isFirstCarrier)
                    isFirstCarrier = false
                    nextTick {
                        if (newCarrier.player.inventory.itemInMainHand.type == tttItem.material) isSelected = true
                    }
                }
            }

        /**
         * This is called after onDeselect
         */
        protected open fun onCarrierRemoved(oldCarrier: TTTPlayer) {}

        /**
         * This is called before isSelected is set to true in the next tick (only if the item is in the main hand)
         */
        protected open fun onCarrierSet(carrier: TTTPlayer, isFirst: Boolean) {}

        protected fun requireCarrier() =
            carrier ?: run {
                throw IllegalStateException("The item must be carried to perform this action")
            }

        /**
         * The reason why the item can not be dropped or null if it can be dropped.
         * Should be overridden with a dynamic getter.
         */
        open val notDroppableReason: String? =
            if (droppable) null
            else "${ChatColor.RED}Du kannst dieses Item nicht droppen"

        open fun onRightClick(event: ClickEvent) { event.isCancelled = false }
        open fun onLeftClick(event: ClickEvent) { event.isCancelled = false }
        open fun onHandSwap() {}

        open fun reset() {}

        var isSelected = false
            set(value) {
                if (value == isSelected) return
                field = value

                if (value) onSelect()
                else onDeselect()
            }

        protected open fun onSelect() {}

        /**
         * If this is called because the carrier is set to null, it is called before the field is changed
         */
        protected open fun onDeselect() {}
    }
}
