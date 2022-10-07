package com.jetbrains.rider.ezargs.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class AppSettingsComponent {
    private val historySizeField = ComboBox(arrayOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)).apply {
        item = AppSettingsState.Instance.historySize
        isEditable = true
    }
    private val mainPanel:JPanel = FormBuilder
        .createFormBuilder()
        .addLabeledComponent(JBLabel("History size:"), historySizeField, 1,false)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    @Suppress("unused")
    fun getPanel() = mainPanel
    fun getPreferredFocusedComponent() = historySizeField
    fun getHistorySize():Int = historySizeField.editor.item as Int
    fun setHistorySize(size:Int) {
        historySizeField.editor.item = size
    }
}