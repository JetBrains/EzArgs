package com.jetbrains.rider.ezargs.ui

import com.intellij.ide.ui.UIDensity
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.project.Project
import com.intellij.ui.NewUI
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel

class CmdlineComboBoxComponentHolder : BorderLayoutPanel() {
    private var wasInitialized = false
    private var onInit: ((CmdlineComboBoxComponent, Project) -> Unit)? = null

    init {
        isOpaque = false
        if (NewUI.isEnabled() && UISettings.getInstance().uiDensity == UIDensity.DEFAULT) {
            border = JBUI.Borders.empty(5, 0)
        }
    }

    fun initialize(project: Project) {
        if (wasInitialized) return
        wasInitialized = true

        val c = CmdlineComboBoxComponent(project)
        addToCenter(c)

        onInit?.let { it(c, project) }
    }

    fun onInit(doAction: (CmdlineComboBoxComponent, Project) -> Unit): CmdlineComboBoxComponentHolder {
        require(onInit == null)
        onInit = doAction

        return this
    }
}