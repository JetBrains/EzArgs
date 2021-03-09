package com.github.decoyrs.ezargs.actions

import com.github.decoyrs.ezargs.EzArgsBundle
import com.github.decoyrs.ezargs.services.EzArgsService
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.ui.EditorComboBox
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.platform.util.project
import javax.swing.JComponent

class ArgumentsInputField : AnAction(), CustomComponentAction {
    override fun createCustomComponent(presentation: Presentation, place: String, context: DataContext): JComponent {
        return EditorComboBox(EzArgsBundle.message("action.EzArgs.ArgumentsInputFieldAction.tooltip")).apply {
            val project = context.project!!
            val service = EzArgsService.getInstance(project)
            setHistory(service.history.toTypedArray())
            service.addHistoryListener(project.lifetime) { history ->
                setHistory(history.toTypedArray())
                val lastElement = model.getElementAt(model.size - 1)
                model.selectedItem = lastElement
            }
            project.lifetime.bracket(
                    { addDocumentListener(service) },
                    { removeDocumentListener(service) }
            )
        }
    }

    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // TODO: focus actions OR show mini text editor with run button
        }
    }
}
