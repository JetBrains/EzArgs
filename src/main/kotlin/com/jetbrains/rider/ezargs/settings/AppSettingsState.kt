package com.jetbrains.rider.ezargs.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name="com.jetbrains.rider.ezargs.settings.AppSettingsState",
    storages= [Storage("EzArgsSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState>, Disposable {
    companion object {
        fun getInstance() = service<AppSettingsState>()
    }

    var historySize = 10
    var width = 250
    var shouldOverwriteRunConfigurationParameters = false

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun dispose() {}
}