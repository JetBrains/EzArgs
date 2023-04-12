package com.jetbrains.rider.ezargs.settings

import com.intellij.openapi.options.Configurable
import com.jetbrains.rider.ezargs.EzArgsBundle
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var appSettingsComponent:AppSettingsComponent? = null
    override fun createComponent(): JComponent {
        appSettingsComponent = AppSettingsComponent()
        return appSettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.getInstance()
        return appSettingsComponent!!.getHistorySize() != settings.historySize ||
                appSettingsComponent!!.getWidth() != settings.width ||
                appSettingsComponent!!.getShouldOverwrite() != settings.shouldOverwriteRunConfigurationParameters

    }

    override fun apply() {
        val settings = AppSettingsState.getInstance()
        settings.historySize = appSettingsComponent!!.getHistorySize()
        settings.width = appSettingsComponent!!.getWidth()
        settings.shouldOverwriteRunConfigurationParameters = appSettingsComponent!!.getShouldOverwrite()
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        appSettingsComponent = null
    }

    override fun reset() {
        val settings = AppSettingsState.getInstance()
        appSettingsComponent!!.setHistorySize(settings.historySize)
        appSettingsComponent!!.setWidth(settings.width)
        appSettingsComponent!!.setShouldOverwrite(settings.shouldOverwriteRunConfigurationParameters)
    }

    override fun getDisplayName() = EzArgsBundle.message("EzArgs.settings.title")
    override fun getPreferredFocusedComponent(): JComponent {
        return appSettingsComponent!!.getPreferredFocusedComponent()
    }
}