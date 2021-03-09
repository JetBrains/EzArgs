package com.github.decoyrs.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.application
import com.jetbrains.rd.util.lifetime.Lifetime
import com.intellij.openapi.editor.event.DocumentListener
import java.util.LinkedList

inline fun <T> kotlin.collections.List<T>.firstOrElse(defaultValue: () -> T): T = firstOrNull() ?: defaultValue()

fun interface HistoryListener {
    fun invoke(history: List<String>)
}

@Service
class EzArgsService(private val project: Project) : DocumentListener {
    companion object {
        const val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
        fun getInstance(project: Project): EzArgsService = project.service()
    }

    private val historyListeners = mutableListOf<HistoryListener>()
    fun addHistoryListener(lifetime: Lifetime, listener: HistoryListener) {
        application.assertIsDispatchThread()
        lifetime.bracket(
                { historyListeners.add(listener) },
                { historyListeners.remove(listener) }
        )
    }

    val history: LinkedList<String> = run {
        val values = PropertiesComponent.getInstance(project).getValues(ARGUMENTS_HISTORY_PROPERTY)
        if (values == null) {
            LinkedList<String>()
        }
        LinkedList(values.toList())
    }
    var arguments = history.firstOrElse { "" }

    fun addToHistory(newArguments: String) {
        application.assertIsDispatchThread()
        val trimmedArgs = newArguments.trim()
        if (trimmedArgs.isEmpty()) return
        if (history.contains(trimmedArgs)) return

        history.addFirst(trimmedArgs)
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
