package com.luisvilaca.nutsandbolts

import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import java.time.LocalDate
import androidx.compose.ui.draw.alpha
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
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.semantics.contentDescription
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

// The Shop: the bay on the left wears whatever is being tried on; at right the picked part, the outfit being tried, the
// category tabs (pinned on a wide window) and the parts grid. A try-on lasts the visit: leaving the Shop clears it.
@Composable
fun ShopScreen(store: Store, ui: Ui, lay: Lay) {
    val s = store.s
    var cat by remember { mutableStateOf("head") }
    val trying = remember { mutableStateMapOf<String, String>() } // slot to part id, for this visit only
    // within a category: what is worn first, then what is owned, then the season's limited parts, then the cheapest;
    // a limited part out of its season shows only once owned
    val items = PARTS.filter { it.slot == cat && forSale(it, s, LocalDate.now()) }.sortedWith(compareBy({ s.equip[it.slot] != it.id }, { it.id !in s.owned }, { !it.limited }, { it.bolts }, { it.nuts }))
    val sel = PART[trying[cat]] ?: PART[s.equip[cat]] ?: items.first()
    val wear = s.equip + trying
    val orbits = derived(store) { orbitsFor(s, LocalDate.now()) }
    val tried = trying.filter { (k, v) -> s.equip[k] != v }.mapNotNull { PART[it.value]?.name }

    val detail = @Composable { m: Modifier ->
        Column(m) {
            Detail(store, ui, sel, Modifier)
            if (tried.isNotEmpty()) Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(t("Trying on: {0}", tried.joinToString()), Modifier.weight(1f, fill = false), style = MaterialTheme.typography.bodyMedium, color = W.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton({ trying.clear() }) { Text(t("Reset"), color = W.text) }
            }
        }
    }
    val tabs = @Composable {
        SecondaryScrollableTabRow(SLOTS.keys.indexOf(cat), containerColor = W.ground, contentColor = W.text, edgePadding = 0.dp, divider = {},
            indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(SLOTS.keys.indexOf(cat)), color = W.act) }) {
            // the tab's press is a rounded tile, not the bare rectangle a Material tab ripples in
            SLOTS.forEach { (k, v) -> Tab(cat == k, { cat = k }, Modifier.padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)), text = { Text(t(v), style = MaterialTheme.typography.labelLarge) }, unselectedContentColor = W.dim) }
        }
    }
    val grid = @Composable {
        BoxWithConstraints(Modifier.padding(top = 16.dp)) {
            val cols = (maxWidth / 156.dp).toInt().coerceAtLeast(2)
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.chunked(cols).forEach { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        r.forEach { p ->
                            Tile(store, p, p.id == sel.id, orbits, Modifier.weight(1f)) { if (s.equip[p.slot] == p.id) trying.remove(p.slot) else trying[p.slot] = p.id }
                        }
                        repeat(cols - r.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header(t("Shop"), store, ui, lay, Modifier.padding(horizontal = 28.dp))
        // the same bay as the Lab, so trying a part on shows it exactly where it will live
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbits, wear)
            Column(Modifier.weight(0.54f).fillMaxHeight().padding(start = 8.dp, end = 32.dp)) {
                detail(Modifier.padding(bottom = 12.dp))
                tabs() // pinned: only the grid scrolls
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) { grid() }
            }
        }
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header(t("Shop"), store, ui, lay)
        // on a phone the bay is shorter here than on the Lab, so the parts start on the first screen
        BayPanel(store, Modifier.padding(vertical = 8.dp).fillMaxWidth().height(if (lay.phone) 260.dp else bayHeight(lay)), orbits, wear, inset = 0.dp)
        detail(Modifier.padding(vertical = 12.dp))
        tabs()
        grid()
    }
}

@Composable
private fun Detail(store: Store, ui: Ui, p: Part, modifier: Modifier) {
    val s = store.s
    val has = p.id in s.owned
    val on = s.equip[p.slot] == p.id
    val need = listOf("bolt" to p.bolts - s.bolts, "nut" to p.nuts - s.nuts).filter { it.second > 0 }
    val words = if (p.slot == "base") "Move in" else "Equip"
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.titleLarge)
            if (p.limited) Text(t("Halloween limited edition"), style = MaterialTheme.typography.bodyMedium, color = HALLOWEEN)
            // owned parts say what they are through the button alone
            if (!has) Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Price(p, false, short = lack(s, p)) }
        }
        val btn = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)
        when {
            !has -> Button(onClick = {
                val before = s.equip[p.slot]
                var ok = false
                store.change { ok = buy(this, p) }
                if (ok) {
                    store.react("cheer")
                    Sfx.play("buy")
                    ui.tell(t("{0} is yours", p.name)) { store.change { unbuy(this, p, before) } }
                }
            }, enabled = need.isEmpty(), modifier = Modifier.height(44.dp), shape = CircleShape, colors = btn) {
                Text(t("Buy"))
                if (need.isEmpty()) {
                    Text(" · ", color = W.onAct)
                    if (p.bolts > 0) { Icon(ICONS.getValue("bolts"), null, Modifier.size(16.dp)); Text("${p.bolts}", Modifier.padding(start = 2.dp)) }
                    if (p.nuts > 0) { Icon(ICONS.getValue("nuts"), null, Modifier.padding(start = 6.dp).size(16.dp)); Text("${p.nuts}", Modifier.padding(start = 2.dp)) }
                }
            }
            !on -> Button(onClick = { store.change { equip[p.slot] = p.id }; Sfx.play("equip") }, Modifier.height(44.dp), shape = CircleShape, colors = btn) { Text(t(words)) }
            p.slot == "acc" -> OutlinedButton(onClick = { store.change { equip["acc"] = null }; Sfx.play("undo") }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text(t("Take off"), color = W.text) }
            // worn: the tile under it already says Equipped (or Living here), so the panel does not say it twice
            else -> {}
        }
    }
}

@Composable
// short: what the balance lacks, in bolts and nuts; the price then reads in dim and says what is needed, so the
// reason is in words, not only in a fainter colour
private fun Price(p: Part, owned: Boolean, short: Pair<Int, Int> = 0 to 0) {
    when {
        owned -> Text(t("Owned"), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        p.bolts == 0 && p.nuts == 0 -> Text(t("Free"), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        else -> Column {
            val lack = short.first > 0 || short.second > 0
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (p.bolts > 0) Coin("bolts", p.bolts, small = true, short = lack); if (p.nuts > 0) Coin("nuts", p.nuts, small = true, short = lack)
            }
            if (lack) Text(listOfNotNull(short.first.takeIf { it > 0 }?.let { t("{0} more", plural(it, "bolt")) },
                short.second.takeIf { it > 0 }?.let { t("{0} more", plural(it, "nut")) }).joinToString(" · "),
                Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun lack(s: Data, p: Part) = (p.bolts - s.bolts).coerceAtLeast(0) to (p.nuts - s.nuts).coerceAtLeast(0)

@Composable
private fun Tile(store: Store, p: Part, sel: Boolean, orbits: List<Orbit>, modifier: Modifier, onClick: () -> Unit) {
    val s = store.s
    val eq = s.equip[p.slot] == p.id
    // one panel per part; the selection ring and the press both sit on the panel itself, not on the words under it
    val press = remember { MutableInteractionSource() }
    Column(modifier.clip(RoundedCornerShape(12.dp)).selectable(sel, interactionSource = press, indication = null, role = Role.RadioButton, onClick = onClick).padding(bottom = 6.dp)) {
        val owned = p.id in s.owned && !eq
        // a base tile draws its whole room once and reuses the picture until the base, the robot or today's bolts change
        val key = if (p.slot == "base") listOf(p.id, s.equip.toMap(), orbits.map { o -> o.h.id to o.slots.map { it.s } }).hashCode() else 0
        val cache = remember { arrayOfNulls<Any>(2) }
        Box {
            Canvas(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(W.raised)
                .background(if (sel) W.selFill else Color.Transparent).border(if (sel) 1.5.dp else 0.dp, if (sel) W.sel else Color.Transparent, RoundedCornerShape(12.dp)).indication(press, ripple())) {
                if (p.slot != "base") thumb(p)
                else {
                    val k = key to size
                    if (cache[0] != k) {
                        val b = ImageBitmap(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1))
                        CanvasDrawScope().draw(this, layoutDirection, androidx.compose.ui.graphics.Canvas(b), size) {
                            room(s, 0L, null, orbits, null, s.equip + ("base" to p.id))
                        }
                        cache[0] = k; cache[1] = b
                    }
                    drawImage(cache[1] as ImageBitmap)
                }
            }
            if (p.limited) Text(t("Limited"), Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(HALLOWEEN).padding(horizontal = 6.dp, vertical = 1.dp),
                style = MaterialTheme.typography.labelMedium, color = Color(0xFF2A1004))
            if (owned) Box(Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp).clip(CircleShape).background(W.card).border(1.dp, W.sel, CircleShape)
                .semantics { contentDescription = t("Owned") }, contentAlignment = Alignment.Center) {
                Icon(ICONS.getValue("check"), null, Modifier.size(14.dp), tint = W.steel)
            }
        }
        Text(p.name, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.padding(top = 4.dp).heightIn(min = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (eq) {
                Icon(ICONS.getValue("check"), null, Modifier.size(16.dp), tint = W.steel)
                Text(t(if (p.slot == "base") "Living here" else "Equipped"), style = MaterialTheme.typography.bodyMedium, color = W.steel)
            } else Price(p, p.id in s.owned, short = lack(s, p))
        }
    }
}
