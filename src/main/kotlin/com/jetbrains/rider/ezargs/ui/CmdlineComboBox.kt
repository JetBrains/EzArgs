package com.jetbrains.rider.ezargs.ui

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.ComboBoxEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.event.DocumentListener

open class CmdlineComboBox: ComboBox<String>() {
    private val borderWidth = 1
    private val myDocumentListeners: MutableList<DocumentListener> = ContainerUtil.createLockFreeCopyOnWriteList()
    val myCmdLineEditor = RawCommandLineEditor()
    val uiDelta = 7
    val toolbarHeight = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + uiDelta)
    val nestedHeight = toolbarHeight - insets.top - insets.bottom - borderWidth * 2
    init {
        enableEvents(8L)
        setHistory(arrayOf(""))
        setEditable(true)
        myCmdLineEditor.isEnabled = true
        myCmdLineEditor.border = BorderFactory.createEmptyBorder()
        myCmdLineEditor.insets.set(0,0,0,0)
        myCmdLineEditor.editorField.border = BorderFactory.createEmptyBorder()
        myCmdLineEditor.editorField.insets.set(0,0,0,0)
        myCmdLineEditor.editorField.background = background
        setEditor(object : ComboBoxEditor {
            override fun getEditorComponent(): Component {
                return myCmdLineEditor
            }

            override fun setItem(anObject: Any?) {
                if(anObject != null) {
                    myCmdLineEditor.text = anObject.toString()
                } else {
                    myCmdLineEditor.text = ""
                }
            }

            override fun getItem(): Any {
                return myCmdLineEditor.text
            }

            override fun selectAll() = myCmdLineEditor.editorField.selectAll()

            override fun addActionListener(l: ActionListener?) { }
            override fun removeActionListener(l: ActionListener?) { }
        })
    }

    override fun getPreferredSize(): Dimension {
        val prefSize = Dimension(myCmdLineEditor.preferredSize.width, toolbarHeight)
        myCmdLineEditor.preferredSize = Dimension(myCmdLineEditor.preferredSize.width, nestedHeight)
        myCmdLineEditor.editorField.preferredSize = Dimension(myCmdLineEditor.preferredSize.width, nestedHeight)
        return prefSize
    }

    fun setHistory(history: Array<String>) {
        model = DefaultComboBoxModel(history)
    }

    fun setText(text:String){
        myCmdLineEditor.editorField.text = text
    }

    fun getText(): String = myCmdLineEditor.editorField.text

    fun addDocumentListener(listener: DocumentListener){
        myDocumentListeners.add(listener)
        myCmdLineEditor.document.addDocumentListener(listener)
    }

    fun clearListeners(){
        myDocumentListeners.forEach { myCmdLineEditor.document.removeDocumentListener(it) }
    }
}