package com.luisvilaca.nutsandbolts

import kotlin.math.min
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

private val CARD = RoundedCornerShape(12.dp)

// A surface card: the one container for form groups, habit rows and calendars. Selected cards take a steel edge.
@Composable
// accent: a 3dp bar of that colour down the card's left edge (a finished habit glows there).
fun Card(modifier: Modifier = Modifier, selected: Boolean = false, pad: Dp = 16.dp, accent: Color? = null, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.clip(CARD).background(W.card).background(if (selected) W.selFill else Color.Transparent)
        .drawWithContent { drawContent(); accent?.let { cardAccent(it) } }
        .border(if (selected) 1.5.dp else 1.dp, if (selected) W.sel else W.border, CARD).padding(pad), content = content)

// A segmented choice whose selection slides between options on a spring; labels may run to three lines at large text.
@Composable
fun Segments(opts: List<Pair<String, String>>, cur: String, modifier: Modifier = Modifier, set: (String) -> Unit) {
    val i = opts.indexOfFirst { it.first == cur }.coerceAtLeast(0)
    val at by animateFloatAsState(i.toFloat(), spring(dampingRatio = 0.8f, stiffness = 500f), label = "segment")
    Row(modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(22.dp)).border(1.dp, W.edge, RoundedCornerShape(22.dp))
        .drawBehind {
            val w = size.width / opts.size
            drawRoundRect(W.selFill, Offset(w * at + 3.dp.toPx(), 3.dp.toPx()), Size(w - 6.dp.toPx(), size.height - 6.dp.toPx()), CornerRadius(size.height / 2))
            drawRoundRect(W.sel, Offset(w * at + 3.dp.toPx(), 3.dp.toPx()), Size(w - 6.dp.toPx(), size.height - 6.dp.toPx()), CornerRadius(size.height / 2), style = Stroke(1.5.dp.toPx()))
        }.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
        opts.forEach { (k, v) ->
            val c by animateColorAsState(if (k == cur) W.text else W.dim, label = "segment label")
            // the press is clipped to the sliding pill's own shape, so it never shows as a square inside the bar
            Text(v, Modifier.weight(1f).padding(3.dp).clip(RoundedCornerShape(19.dp)).selectable(k == cur, role = Role.RadioButton) { set(k) }.padding(horizontal = 5.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge, color = c, textAlign = TextAlign.Center, maxLines = 3) // room for large text
        }
    }
}

// A text field on a faint fill with an outline in the ready colour (W.act) when focused, 12 by 16dp inside.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// password: its characters hidden, and the keyboard's password layout.
fun Field(value: String, set: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, password: Boolean = false) {
    val hide = if (password) PasswordVisualTransformation() else VisualTransformation.None
    val src = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White.copy(alpha = 0.05f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.05f), focusedBorderColor = W.act, unfocusedBorderColor = W.border,
        cursorColor = W.act, focusedTextColor = W.text, unfocusedTextColor = W.text, focusedPlaceholderColor = W.dim, unfocusedPlaceholderColor = W.dim)
    BasicTextField(value, set, modifier.fillMaxWidth(), singleLine = true, interactionSource = src, cursorBrush = SolidColor(W.act), visualTransformation = hide,
        keyboardOptions = if (password) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = W.text)) { inner ->
        OutlinedTextFieldDefaults.DecorationBox(value, inner, enabled = true, singleLine = true, visualTransformation = hide,
            interactionSource = src, placeholder = { Text(placeholder) }, colors = colors, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            container = { OutlinedTextFieldDefaults.Container(enabled = true, isError = false, interactionSource = src, colors = colors, shape = RoundedCornerShape(10.dp)) })
    }
}

// A habit's period, one mark per slot, drawn by the skin (Art.kt: segments in the workshop, a string of bulbs in the
// circus). Slots logged on another day of the week are fainter; the whole tracker lights up once the period is complete.
@Composable
fun Tracker(slots: List<Slot>, modifier: Modifier) = Canvas(modifier) { tracker(slots) }

// A colour chosen by eye: hue round the wheel, strength out from its white centre, brightness on the slider under it;
// the hex value shows as it changes. done gets "#RRGGBB".
@Composable
fun ColorWheel(initial: String, done: (String) -> Unit, close: () -> Unit) {
    val hsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(runCatching { android.graphics.Color.parseColor(initial) }.getOrDefault(-1), it) } }
    var h by remember { mutableFloatStateOf(hsv[0]) }
    var sat by remember { mutableFloatStateOf(hsv[1]) }
    var v by remember { mutableFloatStateOf(hsv[2]) }
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, sat, v))
    val hex = "#%06X".format(argb and 0xFFFFFF)
    fun pick(p: Offset, size: androidx.compose.ui.unit.IntSize) {
        val c = Offset(size.width / 2f, size.height / 2f); val r = min(size.width, size.height) / 2f
        val d = p - c
        h = ((Math.toDegrees(kotlin.math.atan2(d.y, d.x).toDouble()) + 360) % 360).toFloat()
        sat = (d.getDistance() / r).coerceIn(0f, 1f)
    }
    androidx.compose.material3.AlertDialog(onDismissRequest = close, containerColor = W.raised, title = { Text(t("Pick a colour")) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.size(240.dp)
                    .pointerInput(Unit) { detectTapGestures { pick(it, size) } }
                    .pointerInput(Unit) { detectDragGestures { ch, _ -> pick(ch.position, size) } }
                    .semantics { contentDescription = t("Colour wheel") }) {
                    val r = size.minDimension / 2
                    drawCircle(Brush.sweepGradient(listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { Color.hsv(it, 1f, 1f) }, center), r, center)
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0f)), center, r), r, center)
                    drawCircle(Color.Black, r, center, alpha = 1f - v)
                    val a = Math.toRadians(h.toDouble())
                    val m = center + Offset((kotlin.math.cos(a) * sat * r).toFloat(), (kotlin.math.sin(a) * sat * r).toFloat())
                    drawCircle(Color(argb), 12.dp.toPx(), m)
                    drawCircle(Color.White, 12.dp.toPx(), m, style = Stroke(3.dp.toPx()))
                    drawCircle(Color.Black, 13.5.dp.toPx(), m, alpha = 0.5f, style = Stroke(1.dp.toPx()))
                }
                Row(Modifier.padding(top = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(t("Brightness").uppercase(), Modifier.weight(1f), style = Label)
                    Text(hex, style = Numeral.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = W.text)
                }
                androidx.compose.material3.Slider(v, { v = it }, valueRange = 0f..1f,
                    colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = W.act, activeTrackColor = W.act, inactiveTrackColor = W.line))
                Box(Modifier.fillMaxWidth().heightIn(min = 36.dp).clip(RoundedCornerShape(8.dp)).background(Color(argb)).border(1.dp, W.edge, RoundedCornerShape(8.dp)))
            }
        },
        confirmButton = { androidx.compose.material3.TextButton({ done(hex); close() }) { Text(t("Use this colour"), color = W.act) } },
        dismissButton = { androidx.compose.material3.TextButton(close) { Text(t("Cancel"), color = W.text) } })
}
