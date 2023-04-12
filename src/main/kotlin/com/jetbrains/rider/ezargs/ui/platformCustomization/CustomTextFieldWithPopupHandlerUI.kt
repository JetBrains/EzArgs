package com.jetbrains.rider.ezargs.ui.platformCustomization

import com.intellij.icons.AllIcons
import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.ide.ui.laf.darcula.ui.TextFieldWithPopupHandlerUI
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.JBColor
import com.intellij.ui.NewUI
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ReflectionUtil
import com.intellij.util.ui.GraphicsUtil
import java.awt.Color
import java.awt.Graphics
import java.lang.reflect.Field
import javax.swing.JComponent

internal class CustomTextFieldWithPopupHandlerUI : TextFieldWithPopupHandlerUI() {
    private val iconsPrivateField: Field? = try {
        val privateField = ReflectionUtil.findField(CustomTextFieldWithPopupHandlerUI::class.java, null, "icons")
        privateField.isAccessible = true
        privateField
    }
    catch(t: Throwable) {
        thisLogger().error("Icons field not found", t)
        null
    }

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        if (c !is ExpandableTextField) return
        c.caretColor = c.foreground ?: Color.WHITE
    }

    override fun paintSafely(g: Graphics?) {
        val component = component
        if (!component.isOpaque) paintBackground(g)
        val clip = g!!.clip
        super.paintSafely(g)
        val icons = try {
            iconsPrivateField?.get(this)
        }
        catch (t: Throwable) {
            thisLogger().warn("Unable to access icons private field", t)
            null
        } as? LinkedHashMap<String, IconHolder>
        if (!icons.isNullOrEmpty()) {
            g.clip = clip
            for (holder in icons.values) {
                if (holder.icon != null) {
                    if (holder.hovered && holder.isClickable && holder.icon !== AllIcons.Actions.CloseHovered && NewUI.isEnabled()) {
                        GraphicsUtil.setupAAPainting(g)
                        val arc = DarculaUIUtil.BUTTON_ARC.get()
                        g.color = JBColor.namedColor("MainToolbar.Dropdown.hoverBackground", JBColor.background())
                        g.fillRoundRect(
                            holder.bounds.x,
                            holder.bounds.y,
                            holder.bounds.width,
                            holder.bounds.height,
                            arc,
                            arc
                        )
                    }
                    holder.icon.paintIcon(
                        component, g, holder.bounds.x + (holder.bounds.width - holder.icon.iconWidth) / 2,
                        holder.bounds.y + (holder.bounds.height - holder.icon.iconHeight) / 2
                    )
                }
            }
        }
    }
}