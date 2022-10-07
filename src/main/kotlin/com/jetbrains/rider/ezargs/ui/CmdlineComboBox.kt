package com.jetbrains.rider.ezargs.ui

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.ComboBoxEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.event.DocumentListener

open class CmdlineComboBox: ComboBox<String>() {
    val borderWidth = 1
    val myCmdLineEditor = RawCommandLineEditor()
    val myDocumentListeners =
            ContainerUtil.createLockFreeCopyOnWriteList<DocumentListener>()
    init {
        enableEvents(8L)
        setHistory(arrayOf(""))
        setEditable(true)
        myCmdLineEditor.isEnabled = true
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
        val height = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + 5)
        val prefSize = Dimension(myCmdLineEditor.preferredSize.width, height)
        val insets = myCmdLineEditor.border.getBorderInsets(this)
        myCmdLineEditor.preferredSize = Dimension(myCmdLineEditor.preferredSize.width, height - insets.top - insets.bottom - borderWidth * 2)
        return prefSize
    }

    fun setHistory(history: Array<String>) {
        model = DefaultComboBoxModel(history)
    }

    fun setText(text:String){
        myCmdLineEditor.editorField.text = text
    }

    fun getText() = myCmdLineEditor.editorField.text

    fun addDocumentListener(listener: DocumentListener){
        myDocumentListeners.add(listener)
        myCmdLineEditor.document.addDocumentListener(listener)
    }
    fun clearListeners(){
        myDocumentListeners.forEach { myCmdLineEditor.document.removeDocumentListener(it) }
    }

//    private class MyEditor private constructor() : ComboBoxEditor {
//        override fun addActionListener(l: ActionListener) {}
//        override fun getEditorComponent(): Component {
//            return
//        }
//
//        override fun getItem(): Any {
//            return this@EditorComboBox.myDocument.getText()
//        }
//
//        override fun removeActionListener(l: ActionListener) {}
//        override fun selectAll() {
//            if (this@EditorComboBox.myEditorField != null) {
//                val editor: Editor = this@EditorComboBox.myEditorField.getEditor()
//                if (editor != null) {
//                    editor.selectionModel.setSelection(0, this@EditorComboBox.myDocument.getTextLength())
//                }
//            }
//        }
//
//        override fun setItem(anObject: Any) {
//            if (anObject != null) {
//                this@EditorComboBox.setText(anObject.toString())
//            } else {
//                this@EditorComboBox.setText("")
//            }
//        }
//    }

}