package com.jetbrains.rider.ezargs.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFrame
import com.intellij.ui.EditorComboBox
import com.intellij.ui.EditorTextField
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.util.textCompletion.TextCompletionUtil
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.ezargs.completion.CmdArgsCompletionProvider
import com.jetbrains.rider.ezargs.services.EzArgsService
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ArgumentsInputField : AnAction(), DumbAware, CustomComponentAction {
    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // Good first task: focus actions OR show mini text editor with run button
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return object : JPanel(VerticalLayout(JBUI.scale(2))) {
            private var editorComboBox: EditorComboBox? = null
            private var project: Project? = null
                set(value) {
                    if (field == value) return
                    destroy()
                    field = value
                    value?.let {
                        createAndAdd(it)
                    }
                }
            override fun addNotify() {
                super.addNotify()
                (SwingUtilities.getWindowAncestor(this) as? IdeFrame)?.project?.let {
                    project = it
                }
            }
            private fun destroy() {
                editorComboBox?.let {
                    if (components.contains(it)) {
                        remove(it)
                    }
                }
            }
            private fun createAndAdd(project: Project) {
                val service = EzArgsService.getInstance(project)
                val documentCreator = TextCompletionUtil.DocumentWithCompletionCreator(CmdArgsCompletionProvider, true)
                val document = LanguageTextField.createDocument(
                    service.arguments,
                    PlainTextLanguage.INSTANCE,
                    project,
                    documentCreator
                )

                val borderWidth = 1
                val newComboBox = object : EditorComboBox(
                    document,
                    project,
                    PlainTextFileType.INSTANCE
                ) {
                    override fun getPreferredSize(): Dimension {
                        val height = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + 5)
                        val prefSize = Dimension(super.getPreferredSize().width, height)
                        val insets = border.getBorderInsets(this)
                        val editorField = EditorComboBox::class.java.getDeclaredField("myEditorField")
                        editorField.isAccessible = true
                        val editorTextField = editorField.get(this) as EditorTextField
                        editorTextField.component.preferredSize = Dimension(editorTextField.component.preferredSize.width,
                            height - insets.top - insets.bottom - borderWidth * 2)
                        return prefSize
                    }
                }

                project.lifetime.bracket(
                    { newComboBox.addDocumentListener(service) },
                    { newComboBox.removeDocumentListener(service) }
                )
                newComboBox.setHistory(service.history.toTypedArray())
                service.addHistoryListener(project.lifetime) {
                    newComboBox.setHistory(it.toTypedArray())
                }
                newComboBox.selectedItem = service.arguments
                editorComboBox = newComboBox
                add(editorComboBox)
            }
        }
    }
}
