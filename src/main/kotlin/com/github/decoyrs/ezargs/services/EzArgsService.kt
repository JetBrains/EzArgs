package com.github.decoyrs.ezargs.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class EzArgsService(project: Project) {
    companion object {
        val ARGUMENTS_HISTORY_PROPERTY = "ezargs.argumentsList"
    }

    private val history = PropertiesComponent.getInstance(project).getValues(ARGUMENTS_HISTORY_PROPERTY)?.toMutableList()
            ?: mutableListOf<String>()
    private var arguments = history.getOrElse(0) { "" }

    fun AddArgumentsToHistory(command:String) {
        history.add(command)
    }

    fun GetCurrentArguments() = arguments
}