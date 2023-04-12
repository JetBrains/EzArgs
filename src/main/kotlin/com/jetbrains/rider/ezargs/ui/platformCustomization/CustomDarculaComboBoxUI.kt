package com.jetbrains.rider.ezargs.ui.platformCustomization

import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.ide.ui.laf.darcula.DarculaUIUtil.Outline
import com.intellij.ide.ui.laf.darcula.ui.DarculaComboBoxUI
import com.intellij.ui.ClientProperty
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import com.jetbrains.rider.ezargs.ui.CmdlineComboBoxComponent
import java.awt.*
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicArrowButton

internal class CustomDarculaComboBoxUI : DarculaComboBoxUI() {
    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        if (c !is JComponent) return
        val g2 = g.create() as Graphics2D
        val bw = DarculaUIUtil.BW.float
        val r = Rectangle(x, y, width, height)
        try {
            checkFocus()
            if (!DarculaUIUtil.isTableCellEditor(c)) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)
                JBInsets.removeFrom(r, JBUI.insets(1))
                g2.translate(r.x, r.y)
                val lw = DarculaUIUtil.LW.float
                val op = DarculaUIUtil.getOutline(comboBox)
                if (comboBox.isEnabled && op != null) {
                    val color = c.getClientProperty("CustomBorderColor") as? Color ?: DarculaUIUtil.getOutlineColor(true, this.hasFocus)
                    paintOutlineBorderCustomColor(g2, r.width, r.height, DarculaUIUtil.COMPONENT_ARC.getFloat(), true, hasFocus, color)
                } else {
                    if (hasFocus && !DarculaUIUtil.isBorderless(c)) {
                        DarculaUIUtil.paintOutlineBorder(g2, r.width, r.height, DarculaUIUtil.COMPONENT_ARC.getFloat(), true, true, Outline.focus)
                    }
                    paintBorder(c, g2, if (DarculaUIUtil.isBorderless(c)) lw else bw, r, lw, DarculaUIUtil.COMPONENT_ARC.getFloat())
                }
            } else {
                DarculaUIUtil.paintCellEditorBorder(g2, c, r, hasFocus)
            }
        } finally {
            g2.dispose()
        }
    }

    override fun paintBorder(c: Component, g2: Graphics2D, bw: Float, r: Rectangle?, lw: Float, arc: Float) {
        if (c !is JComponent) return
        var arc = arc
        val border: Path2D = Path2D.Float(Path2D.WIND_EVEN_ODD)
        border.append(getOuterShape(r, bw, arc), false)
        arc = if (arc > lw) arc - lw else 0.0f
        border.append(getInnerShape(r, bw, lw, arc), false)
        if (hasFocus && DarculaUIUtil.isBorderless(c)) {
            Outline.focus.setGraphicsColor(g2, true)
        } else {
            g2.color = c.getClientProperty("CustomBorderColor") as? Color ?: DarculaUIUtil.getOutlineColor(true, this.hasFocus)
        }
        g2.fill(border)
    }

    private fun paintOutlineBorderCustomColor(
        g: Graphics2D,
        width: Int,
        height: Int,
        arc: Float,
        symmetric: Boolean,
        hasFocus: Boolean,
        color: Color
    ) {
        g.color = color
        DarculaUIUtil.doPaint(g, width, height, arc, symmetric)
    }

    override fun createArrowButton(): JButton {
        val bg = comboBox.background
        val fg = comboBox.foreground
        val button: JButton = object : BasicArrowButton(SOUTH, bg, fg, fg, fg) {
            override fun paint(g: Graphics) {
                val g2 = g.create() as Graphics2D
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)
                    val r = getArrowButtonRect(this)
                    g2.translate(r.x, r.y)
                    val bw = DarculaUIUtil.BW.float
                    val lw = DarculaUIUtil.LW.float
                    var arc = DarculaUIUtil.COMPONENT_ARC.float
                    arc = if (arc > bw + lw) arc - bw - lw else 0.0f
                    val innerShape: Path2D = Path2D.Float()
                    innerShape.moveTo(lw.toDouble(), lw.toDouble())
                    innerShape.lineTo((r.width - lw - arc).toDouble(), lw.toDouble())
                    innerShape.quadTo((r.width - lw).toDouble(), lw.toDouble(), (r.width - lw).toDouble(), (lw + arc).toDouble())
                    innerShape.lineTo((r.width - lw).toDouble(), (r.height - lw - arc).toDouble())
                    innerShape.quadTo((r.width - lw).toDouble(), (r.height - lw).toDouble(), (r.width - lw - arc).toDouble(), (r.height - lw).toDouble())
                    innerShape.lineTo(lw.toDouble(), (r.height - lw).toDouble())
                    innerShape.closePath()
                    g2.color = comboBox.background // difference from idea
                    g2.fill(innerShape)

                    if (comboBox.isEditable || ClientProperty.isTrue(comboBox, PAINT_VERTICAL_LINE)) {
                        val parentColor = (parent as CmdlineComboBoxComponent).getClientProperty("CustomBorderColor") as? Color
                        g2.color = parentColor ?: DarculaUIUtil.getOutlineColor(comboBox.isEnabled, false)
                        g2.fill(Rectangle2D.Float(0f, lw, DarculaUIUtil.LW.float, r.height - lw * 2))
                    }
                    g2.translate(-r.x, -r.y + JBUI.scale(1))
                    paintArrow(g2, this)
                } finally {
                    g2.dispose()
                }
            }

            override fun getPreferredSize(): Dimension {
                return getArrowButtonPreferredSize(comboBox)
            }
        }
        button.border = JBUI.Borders.empty()
        button.isOpaque = false

        return button
    }
    private fun getArrowButtonRect(button: JButton): Rectangle {
        val result = Rectangle(button.size)
        JBInsets.removeFrom(result, JBUI.insets(1, 0, 1, 1))
        val bw = DarculaUIUtil.BW.get()
        JBInsets.removeFrom(result, Insets(bw, 0, bw, bw))
        return result
    }

    private fun getArrowButtonPreferredSize(comboBox: JComboBox<*>?): Dimension {
        val i = if (comboBox != null) comboBox.insets else JBUI.insets(3)
        val height =
            (if (DarculaUIUtil.isCompact(comboBox)) DarculaUIUtil.COMPACT_HEIGHT.get() else DarculaUIUtil.MINIMUM_HEIGHT.get()) + i.top + i.bottom
        return Dimension(DarculaUIUtil.ARROW_BUTTON_WIDTH.get() + i.left, height)
    }
}