package com.github.decoyrs.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class EzArgsService(private val project: Project) {
    companion object {
        val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
    }

    val history = PropertiesComponent.getInstance(project).getValues(ARGUMENTS_HISTORY_PROPERTY)?.toMutableList()
            ?: mutableListOf<String>()
    var arguments = history.getOrElse(0) { "" }

    fun addToHistory(newArguments:String) {
        if(history.contains(newArguments)) return

        history.add(newArguments)
        PropertiesComponent.getInstance(project).setValues(ARGUMENTS_HISTORY_PROPERTY, history.toTypedArray())
    }
}