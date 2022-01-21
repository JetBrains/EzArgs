package com.jetbrains.rider.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.jetbrains.rd.util.lifetime.Lifetime
import java.util.LinkedList

fun interface HistoryListener {
    fun invoke(history: List<String>)
}

@Service
class EzArgsService(private val project: Project) : DocumentListener {
    companion object {
        const val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
        const val LAST_USED_ARGUMENT_PROPERTY = "ezargs.lastUsed"
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
        val values = PropertiesComponent.getInstance(project).getList(ARGUMENTS_HISTORY_PROPERTY)
            ?: return@run LinkedList<String>()
        return@run LinkedList(values)
    }
    var arguments = PropertiesComponent.getInstance(project).getValue(LAST_USED_ARGUMENT_PROPERTY) ?: ""

    fun addToHistory(newArguments: String) {
        application.assertIsDispatchThread()
        val trimmedArgs = newArguments.trim()
        PropertiesComponent.getInstance(project).setValue(LAST_USED_ARGUMENT_PROPERTY, trimmedArgs)
        if (trimmedArgs.isEmpty()) return

        history.remove(trimmedArgs)
        history.addFirst(trimmedArgs)
        PropertiesComponent.getInstance(project).setList(ARGUMENTS_HISTORY_PROPERTY, history)
        historyListeners.forEach {
            it.invoke(history)
        }
    }

    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        arguments = event.document.text
    }
}
