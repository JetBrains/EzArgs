package com.jetbrains.rider.ezargs.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.jetbrains.rider.ezargs.EzArgsBundle
import javax.swing.JPanel

class AppSettingsComponent {
    private val widthField = ComboBox(arrayOf(250, 275, 300)).apply {
        item = AppSettingsState.Instance.width
        isEditable = true
        this.keySelectionManager
    }

    private val historySizeField = ComboBox(arrayOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)).apply {
        item = AppSettingsState.Instance.historySize
        isEditable = true
    }
    private val shouldOverwriteRunConfigs = JBCheckBox(EzArgsBundle.message("EzArgs.settings.overwriteProgramArguments.checkbox.text")).apply {
        isSelected = AppSettingsState.Instance.shouldOverwriteRunConfigurationParameters
    }

    private val mainPanel:JPanel = FormBuilder
        .createFormBuilder()
        .addLabeledComponent(JBLabel(EzArgsBundle.message("EzArgs.settings.historySize.label.text")), historySizeField, 1,false)
        .addLabeledComponent(JBLabel(EzArgsBundle.message("EzArgs.settings.width.label.text")), widthField, 1,false)
        .addComponent( shouldOverwriteRunConfigs, 1)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    @Suppress("unused")
    fun getPanel() = mainPanel
    fun getPreferredFocusedComponent() = historySizeField
    fun getHistorySize():Int = historySizeField.editor.item as Int
    fun setHistorySize(size:Int) {
        historySizeField.editor.item = size
    }

    fun getWidth(): Int  = widthField.editor.item as Int

    fun setWidth(width:Int) {
        widthField.editor.item = width
    }

    fun getShouldOverwrite(): Boolean = shouldOverwriteRunConfigs.isSelected

    fun setShouldOverwrite(shouldOverwrite: Boolean) {
        shouldOverwriteRunConfigs.isSelected = shouldOverwrite
    }
}