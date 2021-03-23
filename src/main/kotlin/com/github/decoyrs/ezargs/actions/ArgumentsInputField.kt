package com.github.decoyrs.ezargs.actions

import com.github.decoyrs.ezargs.EzArgsBundle
import com.github.decoyrs.ezargs.services.EzArgsService
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorComboBox
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.platform.util.project
import com.jetbrains.rd.util.lifetime.onTermination
import javax.swing.JPanel

class WrappedEditorComboBox(var isInitialized:Boolean = false):JPanel()

class ArgumentsInputField : AnAction(), DumbAware, CustomComponentAction {
    override fun createCustomComponent(presentation: Presentation, place: String, context: DataContext) = WrappedEditorComboBox().apply {
        if(context.project == null) return@apply

        initComponent(context.project!!, this)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val wrappedEditorComboBox = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)
        if (wrappedEditorComboBox !is WrappedEditorComboBox) return
        if (wrappedEditorComboBox.isInitialized) return
        val project = e.project ?: return

        initComponent(project, wrappedEditorComboBox)
    }

    private fun initComponent(project: Project, wrappedEditorComboBox:WrappedEditorComboBox) {
        wrappedEditorComboBox.isInitialized = true
        val editorComboBox = EditorComboBox(EzArgsBundle.message("action.EzArgs.ArgumentsInputFieldAction.tooltip"), project, FileTypes.PLAIN_TEXT)
        project.lifetime.onTermination {
            wrappedEditorComboBox.parent?.remove(wrappedEditorComboBox)
        }
        val service = EzArgsService.getInstance(project)
        project.lifetime.bracket(
                {editorComboBox.addDocumentListener(service)},
                {editorComboBox.removeDocumentListener(service)}
        )
        editorComboBox.setHistory(service.history.toTypedArray())
        service.addHistoryListener(project.lifetime) {
            editorComboBox.setHistory(it.toTypedArray())
        }
        wrappedEditorComboBox.add(editorComboBox)
    }

    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // TODO: focus actions OR show mini text editor with run button
        }
    }
}
