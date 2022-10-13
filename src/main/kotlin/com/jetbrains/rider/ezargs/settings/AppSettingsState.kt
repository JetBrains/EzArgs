package com.jetbrains.rider.ezargs.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name="com.jetbrains.rider.ezargs.settings.AppSettingsState",
    storages= [Storage("EzArgsSettings.xml")]
)
class AppSettingsState:PersistentStateComponent<AppSettingsState> {
    companion object {
        val Instance: AppSettingsState = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }

    var historySize:Int = 10
    var width:Int = 250
    var shouldOverwriteRunConfigurationParameters:Boolean = false
    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}