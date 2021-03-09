package com.github.decoyrs.ezargs.actions

import com.github.decoyrs.ezargs.EzArgsBundle
import com.github.decoyrs.ezargs.services.EzArgsService
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.EditorComboBox
import com.jetbrains.rd.platform.util.lifetime

class WrappedEditorComboBox(var isInitialized:Boolean = false):EditorComboBox(EzArgsBundle.message("action.EzArgs.ArgumentsInputFieldAction.tooltip"))


class ArgumentsInputField : AnAction(), DumbAware, CustomComponentAction {
    override fun createCustomComponent(presentation: Presentation, place: String, context: DataContext) = WrappedEditorComboBox()

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editorComboBox = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)
        if (editorComboBox !is WrappedEditorComboBox) return
        if (editorComboBox.isInitialized) return
        val project = e.project ?: return

        editorComboBox.isInitialized = true
        val service = EzArgsService.getInstance(project)
        project.lifetime.bracket(
                {editorComboBox.addDocumentListener(service)                },
                {editorComboBox.removeDocumentListener(service)}
        )
        editorComboBox.setHistory(service.history.toTypedArray())
        service.addHistoryListener(project.lifetime) {
            editorComboBox.setHistory(it.toTypedArray())
        }
    }

    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // TODO: focus actions OR show mini text editor with run button
        }
    }
}
