package com.github.decoyrs.ezargs.services

import com.jetbrains.rider.run.configurations.exe.ExeConfigurationParameters
import com.jetbrains.rider.cpp.run.configurations.CppConfigurationParametersExtension
import com.intellij.openapi.project.Project

@Suppress("unused")
class EzArgsCppConfigurationParametersExtension(val project:Project) : CppConfigurationParametersExtension {
    override fun process(parameters: ExeConfigurationParameters){
        val service = project.getService(EzArgsService::class.java)
        parameters.programParameters += " " + service.GetCurrentArguments()
        service.AddArgumentsToHistory(service.GetCurrentArguments())
    }
}