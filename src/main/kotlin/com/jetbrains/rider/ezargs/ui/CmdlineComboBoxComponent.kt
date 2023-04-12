package com.jetbrains.rider.ezargs.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.NewUI
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.CurrentTheme.CustomFrameDecorations
import com.jetbrains.rider.ezargs.settings.AppSettingsState
import com.jetbrains.rider.ezargs.ui.platformCustomization.CustomDarculaComboBoxUI
import com.jetbrains.rider.ezargs.ui.platformCustomization.CustomTextFieldWithPopupHandlerUI
import java.awt.Color
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.util.EventListener
import javax.swing.DefaultComboBoxModel
import javax.swing.UIManager
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicComboBoxEditor

fun interface CmdlineComboBoxComponentUpdateListener : EventListener {
    fun onUpdate(newValue: String)
}

class CmdlineComboBoxComponent(private val project: Project) : ComboBox<String>(), Disposable {
    private val myCmdLineEditor = object : ExpandableTextField() {
        fun setUIReal(ui: ComponentUI) {
            super.setUI(ui)
        }
    }

    private val listeners = EventDispatcher.create(CmdlineComboBoxComponentUpdateListener::class.java)

    private val settings get() = AppSettingsState.getInstance()

    private val uiDelta = 7
    private val toolbarHeight = JBUI.scale(ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height + uiDelta)

    private val documentListener: DocumentListener
    private val windowListener: WindowListener
    
    init {
        documentListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = update()
            override fun removeUpdate(e: DocumentEvent?) = update()
            override fun changedUpdate(e: DocumentEvent?) = update()

            private fun update() {
                listeners.multicaster.onUpdate(myCmdLineEditor.text)
            }
        }

        windowListener = object : WindowAdapter() {
            override fun windowActivated(e: WindowEvent?) = updateBackground(true)
            override fun windowDeactivated(e: WindowEvent?) = updateBackground(false)
        }

        isOpaque = false
        myCmdLineEditor.font = EditorUtil.getEditorFont(10)
        myCmdLineEditor.document.addDocumentListener(documentListener)

        enableEvents(8L)
        setEditable(true)

        setEditor(object : BasicComboBoxEditor() {
            override fun getEditorComponent() = myCmdLineEditor
        })

        val frame = WindowManager.getInstance().getFrame(project)
        if (frame == null) {
            thisLogger().warn("Frame for project $project is null")
        }
        frame?.isActive?.let { updateBackground(it) }
        frame?.addWindowListener(windowListener)

        setUI(CustomDarculaComboBoxUI())
        myCmdLineEditor.setUIReal(CustomTextFieldWithPopupHandlerUI() as ComponentUI)

        myCmdLineEditor.background = null
        myCmdLineEditor.isOpaque = false

        Disposer.register(settings, this)
    }

    fun addListener(disposable: Disposable, listener: CmdlineComboBoxComponentUpdateListener) {
        listeners.addListener(listener, disposable)
    }

    override fun getPreferredSize(): Dimension {
        val newWidth = JBUI.scale(settings.width)
        val prefSize = Dimension(newWidth, toolbarHeight - 2)
        //myCmdLineEditor.preferredSize = Dimension(newWidth, prefSize.height-2)
        return prefSize
    }

    private fun updateBackground(active: Boolean) {
        val activeBG = JBColor.namedColor("MainToolbar.background", CustomFrameDecorations.titlePaneBackground())
        val inactiveBG = JBColor.namedColor("MainToolbar.inactiveBackground", activeBG)

        background = when (NewUI.isEnabled()) {
            true -> if (active) activeBG else inactiveBG
            false -> UIManager.getColor("ComboBox.background")
        }
        if (NewUI.isEnabled()) {
            putClientProperty("CustomBorderColor", JBColor(0x4f5257, 0x4f5257).brighter())
        }

        myCmdLineEditor.foreground = JBColor.namedColor("MainToolbar.foreground", Color.WHITE)

        repaint()
    }

    override fun dispose() {
        myCmdLineEditor.document.removeDocumentListener(documentListener)
        WindowManager.getInstance().getFrame(project)?.removeWindowListener(windowListener)
    }

    fun setHistory(history: Array<String>) {
        model = DefaultComboBoxModel(history)
    }

    fun setText(text: String) {
        myCmdLineEditor.text = text
    }

    fun getText(): String = myCmdLineEditor.text
}