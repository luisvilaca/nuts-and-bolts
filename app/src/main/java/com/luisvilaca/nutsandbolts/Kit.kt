package com.luisvilaca.nutsandbolts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource

private val CARD = RoundedCornerShape(12.dp)

// A surface card: the one container for form groups, habit rows and calendars. Selected cards take a steel edge.
@Composable
fun Card(modifier: Modifier = Modifier, selected: Boolean = false, pad: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.clip(CARD).background(W.card).border(if (selected) 1.5.dp else 1.dp, if (selected) W.steel else W.border, CARD).padding(pad), content = content)

// A segmented choice whose selection slides between options on a spring; labels may run to two lines.
@Composable
fun Segments(opts: List<Pair<String, String>>, cur: String, modifier: Modifier = Modifier, set: (String) -> Unit) {
    val i = opts.indexOfFirst { it.first == cur }.coerceAtLeast(0)
    val at by animateFloatAsState(i.toFloat(), spring(dampingRatio = 0.8f, stiffness = 500f), label = "segment")
    Row(modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(22.dp)).border(1.dp, W.edge, RoundedCornerShape(22.dp))
        .drawBehind {
            val w = size.width / opts.size
            drawRoundRect(W.raised, Offset(w * at + 3.dp.toPx(), 3.dp.toPx()), Size(w - 6.dp.toPx(), size.height - 6.dp.toPx()), CornerRadius(size.height / 2))
            drawRoundRect(W.steel, Offset(w * at + 3.dp.toPx(), 3.dp.toPx()), Size(w - 6.dp.toPx(), size.height - 6.dp.toPx()), CornerRadius(size.height / 2), style = Stroke(1.5.dp.toPx()))
        }.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
        opts.forEach { (k, v) ->
            val c by animateColorAsState(if (k == cur) W.text else W.dim, label = "segment label")
            Text(v, Modifier.weight(1f).selectable(k == cur, role = Role.RadioButton) { set(k) }.padding(horizontal = 8.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge, color = c, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

// A text field on a faint fill with a mint outline when focused, 12 by 16dp inside.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Field(value: String, set: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val src = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f),
        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f), focusedBorderColor = W.focus, unfocusedBorderColor = W.border,
        cursorColor = W.focus, focusedTextColor = W.text, unfocusedTextColor = W.text, focusedPlaceholderColor = W.dim, unfocusedPlaceholderColor = W.dim)
    BasicTextField(value, set, modifier.fillMaxWidth(), singleLine = true, interactionSource = src, cursorBrush = SolidColor(W.focus),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = W.text)) { inner ->
        OutlinedTextFieldDefaults.DecorationBox(value, inner, enabled = true, singleLine = true, visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
            interactionSource = src, placeholder = { Text(placeholder) }, colors = colors, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            container = { OutlinedTextFieldDefaults.Container(enabled = true, isError = false, interactionSource = src, colors = colors, shape = RoundedCornerShape(10.dp)) })
    }
}

// A habit's period as segments, one per slot: done charged with the reactor's light, failed in coral, skipped as a
// dashed outline, open as an empty track. Slots logged on another day of the week are fainter.
@Composable
fun Tracker(slots: List<Slot>, modifier: Modifier) = Canvas(modifier) {
    val gap = 4.dp.toPx()
    val w = (size.width - gap * (slots.size - 1)) / slots.size
    val h = size.height
    val r = CornerRadius(h / 2)
    slots.forEachIndexed { i, sl ->
        val o = Offset(i * (w + gap), 0f)
        val a = if (sl.other) 0.5f else 1f
        when (sl.s) {
            "done" -> {
                drawRoundRect(W.glow, o - Offset(0f, h * 0.5f), Size(w, h * 2), CornerRadius(h), alpha = 0.12f * a)
                drawRoundRect(Brush.horizontalGradient(listOf(W.glow, W.steel), o.x, o.x + w), o, Size(w, h), r, alpha = a)
            }
            "fail" -> drawRoundRect(W.coral, o, Size(w, h), r, alpha = a)
            "skip" -> drawRoundRect(W.dim, o, Size(w, h), r, alpha = a, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
            else -> drawRoundRect(W.edge, o, Size(w, h), r)
        }
    }
}
