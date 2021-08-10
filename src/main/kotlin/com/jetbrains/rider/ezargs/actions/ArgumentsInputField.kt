package com.jetbrains.rider.ezargs.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorComboBox
import com.intellij.ui.LanguageTextField
import com.intellij.util.textCompletion.TextCompletionUtil
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.platform.util.project
import com.jetbrains.rd.util.lifetime.onTermination
import com.jetbrains.rider.ezargs.completion.CmdArgsCompletionProvider
import com.jetbrains.rider.ezargs.services.EzArgsService
import javax.swing.JComponent
import javax.swing.JPanel

class WrappedEditorComboBox(var isInitialized: Boolean = false) : JPanel()

class ArgumentsInputField : AnAction(), DumbAware, CustomComponentAction {
    override fun createCustomComponent(presentation: Presentation, place: String, context: DataContext): JComponent {
        return WrappedEditorComboBox().apply {
            if (context.project == null) return@apply

            initComponent(context.project!!, this)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val wrappedEditorComboBox = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY)
        if (wrappedEditorComboBox !is WrappedEditorComboBox) return
        if (wrappedEditorComboBox.isInitialized) return
        val project = e.project ?: return

        initComponent(project, wrappedEditorComboBox)
    }

    private fun initComponent(project: Project, wrappedEditorComboBox: WrappedEditorComboBox) {
        wrappedEditorComboBox.isInitialized = true
        val service = EzArgsService.getInstance(project)
        val documentCreator = TextCompletionUtil.DocumentWithCompletionCreator(CmdArgsCompletionProvider, true)
        val document = LanguageTextField.createDocument(
            service.arguments, PlainTextLanguage.INSTANCE, project,
            documentCreator
        )
        val editorComboBox = EditorComboBox(
            document,
            project,
            PlainTextFileType.INSTANCE
        )
        project.lifetime.onTermination {
            wrappedEditorComboBox.parent?.remove(wrappedEditorComboBox)
        }
        project.lifetime.bracket(
            { editorComboBox.addDocumentListener(service) },
            { editorComboBox.removeDocumentListener(service) }
        )
        editorComboBox.setHistory(service.history.toTypedArray())
        service.addHistoryListener(project.lifetime) {
            editorComboBox.setHistory(it.toTypedArray())
        }
        editorComboBox.selectedItem = service.arguments
        wrappedEditorComboBox.add(editorComboBox)
    }

    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // Good first task: focus actions OR show mini text editor with run button
        }
    }
}
