'use strict';
// Render loop, actions and events. Loads last: the screen files define what it calls.

let reactT;
function render() {
  setHTML($('#wallet'), coin('bolts', S.bolts) + coin('nuts', S.nuts));
  setHTML($('#tabs'), Object.entries(TABS).map(([k, v]) => `<button data-act="tab" data-tab="${k}"${k === ui.tab ? ' aria-current="page"' : ''}>${ICON[k]}${v}</button>`).join(''));
  for (const s of $$('.screen')) s.hidden = s.id !== ui.tab;
  ({ base: renderBase, shop: renderShop, habits: renderHabits, diary: renderDiary })[ui.tab]();
  ui.fresh = null;
  if (ui.react) {
    const bots = $$('.bot:not(.still)'), off = () => bots.forEach(b => b.classList.remove('cheer', 'sad', 'wave'));
    off(); void document.body.offsetWidth; // restart the reaction if one is already playing
    bots.forEach(b => b.classList.add(ui.react));
    ui.react = '';
    clearTimeout(reactT); reactT = setTimeout(off, 1500);
  }
}

let toastT;
function toast(msg) {
  const t = $('#toast');
  t.textContent = msg; t.classList.add('on');
  clearTimeout(toastT); toastT = setTimeout(() => t.classList.remove('on'), 2800);
}

// ---------- actions ----------

const ACT = {
  tab: d => { ui.tab = d.tab; ui.form = null; ui.pop = null; },
  wave: () => { ui.react = 'wave'; },
  mark: d => {
    const h = habit(d.id), m = mark(S, h, ui.date, d.s);
    if (!m) return;
    ui.fresh = h.id;
    ui.react = { done: 'cheer', fail: 'sad' }[d.s] || '';
    const stat = STATS[h.stat].name;
    toast(d.s === 'done' ? `+${plural(m.b, 'bolt')}, +${m.p} ${stat}${m.n ? ', and a nut for the streak' : ''}`
      : d.s === 'fail' ? `Lost ${plural(-m.b, 'bolt')} and ${-m.p} ${stat}` : 'Skipped. Nothing gained, nothing lost.');
  },
  undo: d => { const m = undo(S, habit(d.id), ui.date); if (m) toast(`${STATE[m.s]} mark removed`); },
  edit: d => { ui.form = { id: d.id }; },
  new: () => { ui.tab = 'habits'; ui.form = { id: null }; },
  cancel: () => { ui.form = null; },
  del: () => {
    const h = habit(ui.form.id);
    if (confirm(`Delete "${h.name}"? What it already earned stays.`)) { S.habits = S.habits.filter(x => x !== h); ui.form = null; }
  },
  suggest: d => { S.habits.push({ ...SUGGEST[d.i], id: newId(), start: today() }); },
  step: (d, b) => { const i = b.parentNode.querySelector('input'); i.value = Math.min(7, Math.max(1, (+i.value || 1) + +d.n)); },
  nav: d => {
    if (ui.tab === 'habits' && ui.view === 'day') ui.date = addDays(ui.date, +d.n);
    else ui.month = shiftMonth(ui.month, +d.n);
  },
  today: () => { ui.date = today(); },
  view: d => { ui.view = d.v; ui.month = ui.date.slice(0, 7); },
  day: d => { if (ui.tab === 'diary') openPage(d.date); else { ui.date = d.date; ui.view = 'day'; } },
  cat: d => { ui.cat = d.cat; ui.sel = null; },
  pick: d => { ui.sel = d.id; },
  buy: d => {
    const p = PART[d.id];
    if (S.owned.includes(p.id) || S.bolts < (p.bolts || 0) || S.nuts < (p.nuts || 0)) return;
    S.bolts -= p.bolts || 0; S.nuts -= p.nuts || 0;
    S.owned.push(p.id); S.equip[p.slot] = p.id;
    ui.react = 'cheer';
    toast(`${p.name} is yours`);
  },
  equip: d => { const p = PART[d.id]; if (S.owned.includes(p.id)) S.equip[p.slot] = p.id; },
  unequip: () => { ui.sel = S.equip.acc; S.equip.acc = null; },
  open: () => openPage(today()),
  backup: () => { backup(); },
  back: () => { ui.page = null; ui.pop = null; },
  pop: d => { ui.pop = ui.pop === d.pop ? null : d.pop; },
  // the first tap picks the pen back up from the eraser; a tap on the pen in hand opens its settings
  pen: () => { if (ui.erase) ui.erase = false; else ui.pop = ui.pop === 'pen' ? null : 'pen'; },
  paperReset: () => { S.papers = [...PAPERS]; },
  erase: () => { ui.erase = !ui.erase; ui.pop = null; },
  pundo: () => ink.undo(),
  predo: () => ink.redo(),
  pclear: () => { if (ink.strokes.length && confirm('Bin this page? Everything written on it is erased and cannot be brought back.')) ink.clear(); },
};

// A long press opens a tool's settings (the eraser's width). `held` swallows the click that ends the press.
let holdT, held = false;
document.addEventListener('pointerdown', e => {
  held = false;
  const b = e.target.closest('[data-hold]');
  if (b) holdT = setTimeout(() => { held = true; ui.pop = b.dataset.hold; ui.erase = true; render(); }, 450);
});
for (const ev of ['pointerup', 'pointercancel']) document.addEventListener(ev, () => clearTimeout(holdT));
document.addEventListener('contextmenu', e => { if (e.target.closest('[data-hold]')) e.preventDefault(); });

document.addEventListener('click', e => {
  if (held) { held = false; return; }
  const b = e.target.closest('[data-act]');
  if (ui.pop && !e.target.closest('.pw')) { ui.pop = null; if (!b) return render(); } // a tap anywhere else closes an open popover
  if (!b || b.disabled) return;
  ACT[b.dataset.act](b.dataset, b);
  save(); render();
});
document.addEventListener('submit', e => {
  e.preventDefault();
  const f = Object.fromEntries(new FormData(e.target)), id = ui.form.id || newId();
  const h = { id, name: f.name.trim(), kind: f.kind, per: f.per, target: Math.min(7, Math.max(1, +f.target || 1)), stat: f.stat, weight: f.weight,
    start: f.start || today(), end: f.ends === 'on' ? f.end : null, days: new FormData(e.target).getAll('days').map(Number) };
  if (!h.days.length) return toast('Pick at least one day of the week.');
  if (h.days.length === 7) h.days = null; // every day
  if (!h.name) return;
  if (f.ends === 'on' && !f.end) return toast('Pick the day it ends, or let it run indefinitely.');
  if (h.end && h.end < h.start) return toast('The end day comes before the start day.');
  const i = S.habits.findIndex(x => x.id === id);
  if (i < 0) S.habits.push(h); else S.habits[i] = h;
  ui.form = null;
  save(); render();
});
document.addEventListener('change', e => {
  const t = e.target;
  if (!t.closest('#tools') || !(t.name in S.pen)) return;
  S.pen[t.name] = isNaN(t.value) ? t.value : +t.value;
  if (t.name === 'color') ui.erase = false;
  if (t.name === 'eraser') ui.erase = true;
  if (t.name === 'paper') setPaper(S.papers[t.value]);
  save(); render();
});
// Typing a hex value into a paper slot. No render here: rebuilding the toolbar would drop the caret mid-word.
document.addEventListener('input', e => {
  const t = e.target, i = t.dataset.hex;
  if (i === undefined) return;
  const hex = '#' + t.value.replace(/[^0-9a-f]/gi, '').slice(0, 6).toUpperCase();
  if (hex.length !== 7) return;
  const open = S.papers[i].toLowerCase() === ui.paper.toLowerCase(); // the page on screen uses this slot, so it follows
  S.papers[i] = hex;
  for (const el of $$(`#tools [data-slot="${i}"]`)) el.style.setProperty('--c', hex);
  if (open) { setPaper(hex); applyPaper(); }
  save();
});
$('#botName').addEventListener('input', e => { S.name = e.target.value.trim() || 'Rivet'; save(); });
document.addEventListener('visibilitychange', () => { if (!document.hidden) render(); }); // the date may have rolled over

navigator.storage?.persist?.(); // ask the browser not to evict the diary under storage pressure
if ('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js').catch(() => {});
render();
