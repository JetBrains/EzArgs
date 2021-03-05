package com.github.decoyrs.ezargs.services

import com.intellij.openapi.project.Project
import com.jetbrains.rider.cpp.run.configurations.CppConfigurationParametersExtension
import com.jetbrains.rider.run.configurations.exe.ExeConfigurationParameters

@Suppress("unused")
class EzArgsCppConfigurationParametersExtension(private val project: Project) : CppConfigurationParametersExtension {
    override fun process(parameters: ExeConfigurationParameters) {
        val service = project.getService(EzArgsService::class.java)
        if (parameters.programParameters.isEmpty()) {
            parameters.programParameters = service.arguments
        } else {
            parameters.programParameters += " " + service.arguments
        }
        service.addToHistory(service.arguments)
    }
}
