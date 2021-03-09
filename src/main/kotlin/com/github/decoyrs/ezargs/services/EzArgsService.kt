package com.github.decoyrs.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.application
import com.jetbrains.rd.util.lifetime.Lifetime
import com.intellij.openapi.editor.event.DocumentListener

inline fun <T> kotlin.collections.List<T>.lastOrElse(defaultValue: () -> T): T  = lastOrNull() ?: defaultValue()

fun interface HistoryListener {
    fun invoke(history: List<String>)
}

@Service
class EzArgsService(private val project: Project) : DocumentListener {
    companion object {
        const val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
        fun getInstance(project: Project): EzArgsService  = project.service()
    }

    private val historyListeners = mutableListOf<HistoryListener>()
    fun addHistoryListener(lifetime: Lifetime, listener: HistoryListener) {
        application.assertIsDispatchThread()
        lifetime.bracket(
                { historyListeners.add(listener) },
                { historyListeners.remove(listener) }
        )
    }

    val history = PropertiesComponent.getInstance(project).getValues(ARGUMENTS_HISTORY_PROPERTY)?.toMutableList()
        ?: mutableListOf<String>()
    var arguments = history.lastOrElse { "" }

    fun addToHistory(newArguments: String) {
        application.assertIsDispatchThread()

        if (history.contains(newArguments)) return

        history.add(newArguments)
        PropertiesComponent.getInstance(project).setValues(ARGUMENTS_HISTORY_PROPERTY, history.toTypedArray())
        historyListeners.forEach {
            it.invoke(history)
        }
    }

    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        arguments = event.document.text
    }
}
