package com.jetbrains.rider.ezargs.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.jetbrains.rider.ezargs.ui.CmdlineComboBox
import javax.swing.JComponent

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
        return CmdlineComboBox()
    }
}
