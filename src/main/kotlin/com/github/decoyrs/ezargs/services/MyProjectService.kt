package com.github.decoyrs.ezargs.services

import com.intellij.openapi.project.Project
import com.github.decoyrs.ezargs.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
