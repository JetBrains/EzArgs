package com.jetbrains.rider.ezargs.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.NewUI
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.CurrentTheme.CustomFrameDecorations
import com.jetbrains.rider.ezargs.settings.AppSettingsState
import com.jetbrains.rider.ezargs.ui.platformCustomization.CustomDarculaComboBoxUI
import com.jetbrains.rider.ezargs.ui.platformCustomization.CustomTextFieldWithPopupHandlerUI
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JFrame
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

    private val windowFrame: JFrame?
    
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
        myCmdLineEditor.document.addDocumentListener(documentListener)

        enableEvents(8L)
        setEditable(true)

        setEditor(object : BasicComboBoxEditor() {
            override fun getEditorComponent() = myCmdLineEditor
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
        })

        windowFrame = WindowManager.getInstance().getFrame(project)
        if (windowFrame == null) {
            thisLogger().warn("Frame for project $project is null")
        }
        windowFrame?.isActive?.let { updateBackground(it) }
        windowFrame?.addWindowListener(windowListener)

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
            val isDarkHeader = ColorUtil.isDark(JBColor.namedColor("MainToolbar.background"))
            val darkHeaderBorderColor = JBColor(0x4f5257, 0x4f5257).brighter()
            val lightHeaderBorderColor = JBColor.PanelBackground.darker()
            putClientProperty("CustomBorderColor", if (isDarkHeader) darkHeaderBorderColor else lightHeaderBorderColor)
        }

        myCmdLineEditor.font = EditorUtil.getEditorFont(12)
        myCmdLineEditor.foreground = JBColor.namedColor("MainToolbar.foreground", JBColor.foreground())

        repaint()
    }

    override fun updateUI() {
        super.updateUI()
        if (myCmdLineEditor != null) { // Don't remove: for some reason Swing calls updateUI earlier than class constructor
            updateBackground(windowFrame?.isActive ?: true)
        }
    }

    override fun dispose() {
        myCmdLineEditor.document.removeDocumentListener(documentListener)
        WindowManager.getInstance().getFrame(project)?.removeWindowListener(windowListener)
    }

    fun setHistory(history: Array<String>) {
        val newModel = DefaultComboBoxModel(history)
        newModel.insertElementAt("", 0) // Add blank option to top of combo box

        model = newModel
    }

    fun setText(text: String) {
        myCmdLineEditor.text = text
    }
}