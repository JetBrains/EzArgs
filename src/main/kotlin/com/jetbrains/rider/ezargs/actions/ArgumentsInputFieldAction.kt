package com.jetbrains.rider.ezargs.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.createLifetime
import com.jetbrains.rider.ezargs.services.EzArgsService
import com.jetbrains.rider.ezargs.ui.CmdlineComboBoxComponentHolder
import javax.swing.JComponent

class ArgumentsInputFieldAction : AnAction(), DumbAware, CustomComponentAction {
    companion object {
        private val PROJECT_KEY = com.intellij.openapi.util.Key.create<Project>("ArgumentsInputFieldActionProject")
    }
    override fun actionPerformed(p0: AnActionEvent) {
        if (p0.place == ActionPlaces.KEYBOARD_SHORTCUT) {
            // Good first task: focus actions OR show mini text editor with run button
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
        e.project?.let { e.presentation.putClientProperty(PROJECT_KEY, it) }
    }

    override fun createCustomComponent(presentation: Presentation, place: String) =
        CmdlineComboBoxComponentHolder().onInit { c, project ->
            val service = EzArgsService.getInstance(project)
            c.setHistory(service.history.toTypedArray())
            c.setText(service.arguments)
            service.addHistoryListener(service.createLifetime()) {
                c.setHistory(it.toTypedArray())
            }
        }

    override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
        presentation.getClientProperty(PROJECT_KEY)?.let { (component as CmdlineComboBoxComponentHolder).initialize(it) }
    }
}
