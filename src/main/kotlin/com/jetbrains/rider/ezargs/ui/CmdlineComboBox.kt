package com.jetbrains.rider.ezargs.ui

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.IdeFrame
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.ezargs.services.EzArgsService
import com.jetbrains.rider.ezargs.settings.AppSettingsState
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.ComboBoxEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

open class CmdlineComboBox: ComboBox<String>() {
    private val borderWidth = 1
    private val myDocumentListeners: MutableList<DocumentListener> = ContainerUtil.createLockFreeCopyOnWriteList()
    val myCmdLineEditor = RawCommandLineEditor()
    private val uiDelta = 7
    private val toolbarHeight = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + uiDelta)
    private val nestedHeight = JBUI.scale(toolbarHeight - insets.top - insets.bottom - borderWidth * 2)

    private var project: Project? = null
        set(value) {
            if (field == value) return
            destroy()
            field = value
            value?.let {
                create(it)
            }
        }
    override fun addNotify() {
        super.addNotify()
        (SwingUtilities.getWindowAncestor(this) as? IdeFrame)?.project?.let {
            project = it
        }
    }
    private fun destroy() {
        if (components.contains(this)) {
            remove(this)
        }
    }
    private fun create(project: Project) {
        val service = EzArgsService.getInstance(project)
        val parent = this
        val settings = AppSettingsState.Instance
        val newWidth = JBUI.scale(settings.width)
        myCmdLineEditor.preferredSize = Dimension(newWidth, nestedHeight)
        myCmdLineEditor.editorField.preferredSize = Dimension(newWidth, nestedHeight)
        project.lifetime.bracketIfAlive(
            { addDocumentListener(object: DocumentListener{
                override fun insertUpdate(e: DocumentEvent?) {
                    service.arguments = parent.getText()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    service.arguments = parent.getText()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    service.arguments = parent.getText()
                }
            }) },
            { clearListeners() }
        )
        setHistory(service.history.toTypedArray())
        service.addHistoryListener(project.lifetime) {
            setHistory(it.toTypedArray())
        }
        setText(service.arguments)
    }
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
        val settings = AppSettingsState.Instance
        val newWidth = JBUI.scale(settings.width)
        val prefSize = Dimension(newWidth, toolbarHeight)
        myCmdLineEditor.preferredSize = Dimension(newWidth, nestedHeight)
        myCmdLineEditor.editorField.preferredSize = Dimension(newWidth, nestedHeight)
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