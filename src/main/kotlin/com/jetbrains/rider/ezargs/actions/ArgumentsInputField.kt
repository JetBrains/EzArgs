package com.jetbrains.rider.ezargs.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFrame
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.ezargs.services.EzArgsService
import com.jetbrains.rider.ezargs.ui.CmdlineComboBox
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ArgumentsInputField : AnAction(), DumbAware, CustomComponentAction {
    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // Good first task: focus actions OR show mini text editor with run button
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return object:CmdlineComboBox() {
            private var project: Project? = null
                set(value) {
                    if (field == value) return
                    destroy()
                    field = value
                    value?.let {
                        create(it)
                    }
                }
            override fun addNotify() {
                super.addNotify()
                (SwingUtilities.getWindowAncestor(this) as? IdeFrame)?.project?.let {
                    project = it
                }
            }
            private fun destroy() {
                if (components.contains(this)) {
                    remove(this)
                }
            }
            private fun create(project: Project) {
                val service = EzArgsService.getInstance(project)
                val parent = this
                project.lifetime.bracketIfAlive(
                    { addDocumentListener(object: DocumentListener{
                        override fun insertUpdate(e: DocumentEvent?) {
                            service.arguments = parent.getText()
                        }

                        override fun removeUpdate(e: DocumentEvent?) {
                            service.arguments = parent.getText()
                        }

                        override fun changedUpdate(e: DocumentEvent?) {
                            service.arguments = parent.getText()
                        }
                    }) },
                    { clearListeners() }
                )
                setHistory(service.history.toTypedArray())
                service.addHistoryListener(project.lifetime) {
                    setHistory(it.toTypedArray())
                }
                setText(service.arguments)
            }
        }
    }

//    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
//        return object : JPanel(VerticalLayout(JBUI.scale(2))) {
//            private var cmdLineEditor:RawCommandLineEditor? = null
//            private var editorComboBox: EditorComboBox? = null
//            private var project: Project? = null
//                set(value) {
//                    if (field == value) return
//                    destroy()
//                    field = value
//                    value?.let {
//                        createAndAdd(it)
//                    }
//                }
//            override fun addNotify() {
//                super.addNotify()
//                (SwingUtilities.getWindowAncestor(this) as? IdeFrame)?.project?.let {
//                    project = it
//                }
//            }
//            private fun destroy() {
//                cmdLineEditor?.let {
//                    if (components.contains(it)) {
//                        remove(it)
//                    }
//                }
//            }
//            private fun createAndAdd(project: Project) {
//                val service = EzArgsService.getInstance(project)
//                val documentCreator = TextCompletionUtil.DocumentWithCompletionCreator(CmdArgsCompletionProvider, true)
//                val document = LanguageTextField.createDocument(
//                    service.arguments,
//                    PlainTextLanguage.INSTANCE,
//                    project,
//                    documentCreator
//                )
//
//                val borderWidth = 1
//                val newComboBox = RawCommandLineEditor()
//                val newComboBox = object : EditorComboBox(
//                    document,
//                    project,
//                    PlainTextFileType.INSTANCE
//                ) {
//                    override fun getPreferredSize(): Dimension {
//                        val height = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + 5)
//                        val prefSize = Dimension(super.getPreferredSize().width, height)
//                        val insets = border.getBorderInsets(this)
//                        val editorField = EditorComboBox::class.java.getDeclaredField("myEditorField")
//                        editorField.isAccessible = true
//                        val editorTextField = editorField.get(this) as EditorTextField
//                        editorTextField.component.preferredSize = Dimension(editorTextField.component.preferredSize.width,
//                            height - insets.top - insets.bottom - borderWidth * 2)
//                        return prefSize
//                    }
//                }
//
//                project.lifetime.bracketIfAlive(
//                    { newComboBox.addDocumentListener(service) },
//                    { newComboBox.removeDocumentListener(service) }
//                )
//                newComboBox.setHistory(service.history.toTypedArray())
//                service.addHistoryListener(project.lifetime) {
//                    newComboBox.setHistory(it.toTypedArray())
//                }
//                newComboBox.selectedItem = service.arguments
//                cmdLineEditor = newComboBox
//                add(cmdLineEditor)
//            }
//        }
//    }
}
