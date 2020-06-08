package de.moritzruth.spigot_ttt.game.players

import com.google.common.collect.MutableClassToInstanceMap
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

interface IState

class StateContainer(private val tttPlayer: TTTPlayer) {
    private val instances = MutableClassToInstanceMap.create<IState>()

    fun <T: IState> getOrCreate(stateClass: KClass<T>): T =
        get(stateClass) ?: run {
            val parameterlessConstructor = stateClass.constructors
                .find { it.parameters.isEmpty() && it.visibility == KVisibility.PUBLIC }
                ?: throw NoSuchMethodException("The stateClass has no public parameterless constructor")

            parameterlessConstructor.call().also { instances[stateClass.java] = it }
        }

    fun <T: IState> get(stateClass: KClass<T>): T? = instances.getInstance(stateClass.java)

    fun <T: IState> has(stateClass: KClass<T>): Boolean = instances.containsKey(stateClass.java)

    fun <T: IState> put(stateClass: KClass<out T>, value: T) {
        if (instances.containsKey(stateClass.java))
            throw IllegalStateException("There is already a state instance in this container")

        instances[stateClass.java] = value
    }

    fun <T: IState> remove(stateClass: KClass<T>) = instances.remove(stateClass.java)
}

class InversedStateContainer<T: IState>(private val stateClass: KClass<T>) {
    fun getOrCreate(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.getOrCreate(stateClass)
    fun get(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(stateClass)
    fun remove(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.remove(stateClass)

    val tttPlayers get() = PlayerManager.tttPlayers.filter { it.stateContainer.has(stateClass) }

    fun forEveryState(fn: (T, TTTPlayer) -> Unit) {
        tttPlayers.forEach { it.stateContainer.get(stateClass)?.run { fn(this, it) } }
    }
}
