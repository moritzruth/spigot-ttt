package de.moritzruth.spigot_ttt.game.players

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

interface IState {
}

class StateContainer {
    private val instances = mutableSetOf<IState>()

    @Suppress("UNCHECKED_CAST")
    fun <T: IState> get(stateClass: KClass<T>): T =
        instances.find { state -> stateClass.isInstance(state) } as T? ?: run {
            val parameterlessConstructor = stateClass.constructors
                .find { it.parameters.size == 0 && it.visibility == KVisibility.PUBLIC }
                ?: throw NoSuchMethodException("The stateClass has no public parameterless constructor")

            parameterlessConstructor.call().also { instances.add(it) }
        }

    fun clear() {
        instances.clear()
    }
}

class InversedStateContainer<T: IState>(private val stateClass: KClass<T>) {
    fun get(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(stateClass)
}
