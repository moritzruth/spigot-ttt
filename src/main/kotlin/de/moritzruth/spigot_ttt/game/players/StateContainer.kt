package de.moritzruth.spigot_ttt.game.players

import kotlin.reflect.KClass

interface State

class StateContainer {
    private val instances = mutableSetOf<State>()

    @Suppress("UNCHECKED_CAST")
    fun <T: State> get(stateClass: KClass<T>, createInstance: () -> T): T {
        return (instances.find { stateClass.isInstance(it) } ?: createInstance().also {
            instances.add(it)
        }) as T
    }

    fun clear() {
        instances.clear()
    }
}
