package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ShopScreen(store: Store, ui: Ui, lay: Lay) {
    val s = store.s
    var cat by remember { mutableStateOf("head") }
    var pick by remember { mutableStateOf<String?>(null) }
    val items = PARTS.filter { it.slot == cat }
    val sel = PART[pick]?.takeIf { it.slot == cat } ?: PART[s.equip[cat]] ?: items.first()
    val tryOn = s.equip + (cat to sel.id)

    val detail = @Composable { m: Modifier -> Detail(store, ui, sel, m) }
    val catalog = @Composable {
        SecondaryScrollableTabRow(SLOTS.keys.indexOf(cat), containerColor = W.ground, contentColor = W.text, edgePadding = 0.dp, divider = {},
            indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(SLOTS.keys.indexOf(cat)), color = W.act) }) {
            SLOTS.forEach { (k, v) -> Tab(cat == k, { cat = k; pick = null }, text = { Text(v, style = MaterialTheme.typography.labelLarge) }, unselectedContentColor = W.dim) }
        }
        BoxWithConstraints(Modifier.padding(top = 16.dp)) {
            val cols = (maxWidth / 156.dp).toInt().coerceAtLeast(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.chunked(cols).forEach { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        r.forEach { Tile(store, it, it.id == sel.id, Modifier.weight(1f)) { pick = it.id } }
                        repeat(cols - r.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header("Shop", null, store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(0.46f).fillMaxHeight()) {
                BayPanel(store, Modifier.weight(1f).fillMaxWidth(), orbitsFor(store.s, LocalDate.now()), tryOn, plate = false)
                detail(Modifier.padding(start = 28.dp, end = 12.dp, bottom = 20.dp))
            }
            Column(Modifier.weight(0.54f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 32.dp, bottom = 24.dp)) { catalog() }
        }
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Shop", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(300.dp), orbitsFor(store.s, LocalDate.now()), tryOn, plate = false)
        detail(Modifier.padding(vertical = 12.dp))
        catalog()
    }
}

@Composable
private fun Detail(store: Store, ui: Ui, p: Part, modifier: Modifier) {
    val s = store.s
    val has = p.id in s.owned
    val on = s.equip[p.slot] == p.id
    val need = listOf("bolts" to p.bolts - s.bolts, "nuts" to p.nuts - s.nuts).filter { it.second > 0 }
        .joinToString(" and ") { (k, n) -> "$n more ${if (n == 1) k.dropLast(1) else k}" }
    val words = if (p.slot == "base") "Move in" to "Living here" else "Equip" to "Equipped"
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.titleLarge)
            // owned parts say what they are through the button alone
            if (!has) Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Price(p, false) }
        }
        val btn = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)
        when {
            !has -> Button(onClick = {
                val before = s.equip[p.slot]
                var ok = false
                store.change { ok = buy(this, p) }
                if (ok) {
                    store.react("cheer")
                    ui.tell("${p.name} is yours") { store.change { owned -= p.id; bolts += p.bolts; nuts += p.nuts; equip[p.slot] = before } }
                }
            }, enabled = need.isEmpty(), modifier = Modifier.height(48.dp), shape = CircleShape, colors = btn) { Text(if (need.isEmpty()) "Buy" else "Need $need") }
            !on -> Button(onClick = { store.change { equip[p.slot] = p.id } }, Modifier.height(48.dp), shape = CircleShape, colors = btn) { Text(words.first) }
            p.slot == "acc" -> OutlinedButton(onClick = { store.change { equip["acc"] = null } }, Modifier.height(48.dp), border = BorderStroke(1.dp, W.edge)) { Text("Take off", color = W.text) }
            else -> Text(words.second, style = MaterialTheme.typography.labelLarge, color = W.act)
        }
    }
}

@Composable
private fun Price(p: Part, owned: Boolean) {
    when {
        owned -> Text("Owned", style = MaterialTheme.typography.bodyMedium, color = W.dim)
        p.bolts == 0 && p.nuts == 0 -> Text("Free", style = MaterialTheme.typography.bodyMedium, color = W.dim)
        else -> { if (p.bolts > 0) Coin("bolts", p.bolts, small = true); if (p.nuts > 0) Coin("nuts", p.nuts, small = true) }
    }
}

@Composable
private fun Tile(store: Store, p: Part, sel: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val s = store.s
    val eq = s.equip[p.slot] == p.id
    // one panel per part; the selection ring sits on the panel itself
    Column(modifier.clip(RoundedCornerShape(12.dp)).clickable(role = Role.Tab, onClick = onClick).semantics { selected = sel }.padding(bottom = 6.dp)) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(W.raised)
            .border(if (sel) 2.dp else 0.dp, if (sel) W.steel else Color.Transparent, RoundedCornerShape(12.dp))) { thumb(p) }
        Text(p.name, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.padding(top = 4.dp).height(22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (eq) Text(if (p.slot == "base") "Living here" else "Equipped", style = MaterialTheme.typography.bodyMedium, color = W.act) else Price(p, p.id in s.owned)
        }
    }
}
