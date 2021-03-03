package com.github.decoyrs.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

inline fun <T> kotlin.collections.List<T>.lastOrElse( defaultValue: () -> T): T {
    return try {
        last()
    } catch (e:NoSuchElementException) {
        defaultValue()
    }
}

fun interface HistoryListener {
    fun invoke(history:List<String>)
}

@Service
class EzArgsService(private val project: Project) {
    companion object {
        const val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
    }

    private val historyListeners = mutableListOf<HistoryListener>()
    fun addHistoryListener(listener: HistoryListener) = historyListeners.add(listener)

    val history = PropertiesComponent.getInstance(project).getValues(ARGUMENTS_HISTORY_PROPERTY)?.toMutableList()
            ?: mutableListOf<String>()
    var arguments = history.lastOrElse { "" }

    fun addToHistory(newArguments:String) {
        if(history.contains(newArguments)) return

        history.add(newArguments)
        PropertiesComponent.getInstance(project).setValues(ARGUMENTS_HISTORY_PROPERTY, history.toTypedArray())
        historyListeners.forEach {
            it.invoke(history)
        }
    }
}