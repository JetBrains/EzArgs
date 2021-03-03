package com.github.decoyrs.ezargs.actions

import com.github.decoyrs.ezargs.services.EzArgsService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.EditorComboBox
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class WrappedEditorComboBox(text: String, var isInitialized: Boolean = false) : EditorComboBox(text)

class ArgumentsInputField : AnAction(), CustomComponentAction, DumbAware, ActionListener {
    override fun createCustomComponent(presentation: Presentation, place: String) = WrappedEditorComboBox("Run Arguments").apply { addActionListener(this) }

    override fun update(e: AnActionEvent) {

//        val editorComboBox:WrappedEditorComboBox = e.presentation.getClientProperty(CustomComponentAction.COMPONENT_KEY) as WrappedEditorComboBox
//        if(editorComboBox.isInitialized) {
//            val project = e.project?:return
//            val service: EzArgsService = project.service()
//            editorComboBox.setHistory(service.history.toTypedArray())
//            editorComboBox.isInitialized = true
//            editorComboBox.addActionListener {
//                println(it.actionCommand)
//                service.arguments = editorComboBox.text
//            }
//        }

    }

    override fun actionPerformed(e: AnActionEvent) {

    }
    
    override fun actionPerformed(e: ActionEvent?) {
        TODO("Not yet implemented")
    }
}