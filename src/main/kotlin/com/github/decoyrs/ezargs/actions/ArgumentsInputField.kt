package com.github.decoyrs.ezargs.actions

import com.github.decoyrs.ezargs.EzArgsBundle
import com.github.decoyrs.ezargs.services.EzArgsService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorComboBox

class WrappedEditorComboBox(var isInitialized: Boolean = false) :
    EditorComboBox(EzArgsBundle.message("action.ArgumentsInputField.tooltip"))

class ArgumentsInputField : AnAction(), CustomComponentAction, DocumentListener {
    private var project: Project? = null

    override fun createCustomComponent(presentation: Presentation, place: String) = WrappedEditorComboBox()

    override fun update(e: AnActionEvent) {
        val editorComboBox = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)
        if (editorComboBox !is WrappedEditorComboBox) return
        if (editorComboBox.isInitialized) return
        project = e.project ?: return

        editorComboBox.addDocumentListener(this)
        val service = project!!.getService(EzArgsService::class.java)
        editorComboBox.setHistory(service.history.toTypedArray())
        editorComboBox.isInitialized = true
        service.addHistoryListener {
            editorComboBox.setHistory(it.toTypedArray())
            val lastElement = editorComboBox.model.getElementAt(editorComboBox.model.size - 1)
            editorComboBox.model.selectedItem = lastElement
        }
    }

    override fun actionPerformed(p0: AnActionEvent) {
        TODO("Not yet implemented")
    }

    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        if (project == null) return

        val service = project!!.getService(EzArgsService::class.java)
        service.arguments = event.document.text
    }
}
