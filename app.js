'use strict';
const $ = s => document.querySelector(s);
const $$ = s => document.querySelectorAll(s);
const esc = s => String(s).replace(/[&<>"']/g, c => `&#${c.charCodeAt(0)};`);
// skips untouched regions so running animations and half-filled forms survive a render
const setHTML = (el, html) => { if (el._h !== html) { el.innerHTML = html; el._h = html; } };
const today = () => iso(new Date());
const fmt = (s, o) => day(s).toLocaleDateString('en-GB', o);
const plural = (n, w) => `${n} ${w}${n === 1 ? '' : 's'}`;

const ICON = (() => {
  const i = d => `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true">${d}</svg>`;
  const hex = '<path d="M12 2.8l8 4.6v9.2l-8 4.6-8-4.6V7.4z"/>';
  return {
    bolts: i('<circle cx="12" cy="12" r="8.5"/><path d="M8 12h8M12 8v8"/>'),
    nuts: i(hex + '<circle cx="12" cy="12" r="3.6"/>'),
    battery: i('<rect x="3" y="7.5" width="16" height="9" rx="2.5"/><path d="M21.5 10.5v3M7 10.5v3M10.5 10.5v3"/>'),
    processor: i('<rect x="6.5" y="6.5" width="11" height="11" rx="2"/><path d="M10 3v3.5M14 3v3.5M10 17.5V21M14 17.5V21M3 10h3.5M3 14h3.5M17.5 10H21M17.5 14H21"/>'),
    chassis: i('<rect x="5.5" y="4" width="13" height="16" rx="4"/><path d="M9.5 9h5M9.5 15h5"/>'),
    core: i('<path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.4A4 4 0 0 1 19 10c0 5.6-7 10-7 10z"/>'),
    base: i('<rect x="5" y="8.5" width="14" height="11" rx="3.5"/><path d="M12 8.5V5M9.5 13v1.5M14.5 13v1.5"/><circle cx="12" cy="4" r="1"/>'),
    shop: i('<path d="M4 8.5l8-4.5 8 4.5v7L12 20l-8-4.5z"/><path d="M4 8.5l8 4.5 8-4.5M12 13v7"/>'),
    habits: i(hex + '<path d="M8.5 12.2l2.4 2.4 4.6-5"/>'),
    diary: i('<path d="M4 20l1.2-4.6L16.4 4.2a2 2 0 0 1 2.8 0l.6.6a2 2 0 0 1 0 2.8L8.6 18.8z"/><path d="M14.5 6l3.5 3.5"/>'),
    prev: i('<path d="M14.5 5.5 8 12l6.5 6.5"/>'),
    next: i('<path d="M9.5 5.5 16 12l-6.5 6.5"/>'),
    plus: i('<path d="M12 5v14M5 12h14"/>'),
    minus: i('<path d="M5 12h14"/>'),
    undo: i('<path d="M8 5 4 9l4 4"/><path d="M4 9h10a6 6 0 0 1 0 12h-3"/>'),
    redo: i('<path d="m16 5 4 4-4 4"/><path d="M20 9H10a6 6 0 0 0 0 12h3"/>'),
    eraser: i('<path d="M4.5 15.5 13 7l6 6-6.5 6.5H9z"/><path d="M9.5 10.5l6 6M4 19.5h16"/>'),
    trash: i('<path d="M5 7h14M10 7V4.5h4V7M7 7l.8 12.5h8.4L17 7M10.5 11v5M13.5 11v5"/>'),
  };
})();

const TABS = { base: 'Base', shop: 'Shop', habits: 'Habits', diary: 'Diary' };
const STATE = { done: 'Done', skip: 'Skipped', fail: 'Failed' };
const BTN = { good: { done: 'Done', skip: 'Skip', fail: 'Fail' }, bad: { done: 'Resisted', skip: 'Skip', fail: 'Gave in' } };
const WORDS = { base: ['Move in', 'Living here'], other: ['Equip', 'Equipped'] };
const SUGGEST = [
  { name: 'Drink water', kind: 'good', per: 'day', target: 3, stat: 'battery', weight: 'light' },
  { name: 'Read 20 minutes', kind: 'good', per: 'day', target: 1, stat: 'processor', weight: 'medium' },
  { name: 'Workout', kind: 'good', per: 'week', target: 3, stat: 'chassis', weight: 'heavy' },
  { name: 'Call a friend', kind: 'good', per: 'week', target: 1, stat: 'core', weight: 'medium' },
  { name: 'No phone in bed', kind: 'bad', per: 'day', target: 1, stat: 'battery', weight: 'medium' },
];

// ---------- state ----------

const KEY = 'nb-state-v1';
const FRESH = () => ({
  name: 'Rivet', bolts: 0, nuts: 0,
  stats: { battery: 30, processor: 30, chassis: 30, core: 30 },
  owned: ['head-box', 'body-crate', 'arms-clamp', 'legs-stub', 'base-bay'],
  equip: { head: 'head-box', body: 'body-crate', arms: 'arms-clamp', legs: 'legs-stub', acc: null, base: 'base-bay' },
  habits: [], marks: {}, diary: {},
  pen: { color: PEN_COLORS[0][1], size: 1, stab: 1, paper: 'dark' },
});
function load() {
  const raw = localStorage.getItem(KEY);
  if (!raw) return FRESH();
  try {
    const s = Object.assign(FRESH(), JSON.parse(raw));
    s.pen = { ...FRESH().pen, ...s.pen };
    if (!PEN_COLORS.some(c => c[1] === s.pen.color)) s.pen.color = PEN_COLORS[0][1]; // a colour from an older palette
    return s;
  }
  catch { localStorage.setItem(KEY + '-unreadable', raw); return FRESH(); } // keep the bad copy instead of overwriting it
}
function save() {
  try { localStorage.setItem(KEY, JSON.stringify(S)); }
  catch { toast('Could not save. The tablet is out of storage for this app.'); }
}
let S = load();
const ui = { tab: 'base', date: today(), view: 'day', month: today().slice(0, 7), cat: 'head', sel: null, form: null, page: null, paper: 'dark', erase: false, fresh: null, react: '' };
const habit = id => S.habits.find(h => h.id === id);
const newId = () => 'h' + Date.now().toString(36) + Math.random().toString(36).slice(2, 5);

// Diary pages live in IndexedDB: handwriting outgrows localStorage within weeks.
const db = new Promise((res, rej) => {
  const r = indexedDB.open('nb-diary', 1);
  r.onupgradeneeded = () => r.result.createObjectStore('pages');
  r.onsuccess = () => res(r.result);
  r.onerror = () => rej(r.error);
});
const idb = (mode, fn) => db.then(d => new Promise((res, rej) => {
  const rq = fn(d.transaction('pages', mode).objectStore('pages'));
  rq.onsuccess = () => res(rq.result);
  rq.onerror = () => rej(rq.error);
}));

// ---------- shared pieces ----------

const coin = (k, n) => `<span class="coin ${k}" aria-label="${plural(n, k.slice(0, -1))}">${ICON[k]}<b>${n}</b></span>`;
const price = p => (p.bolts ? coin('bolts', p.bolts) : '') + (p.nuts ? coin('nuts', p.nuts) : '') || 'Free';
const mood = () => {
  const a = Object.values(S.stats).reduce((x, y) => x + y) / 4;
  return a >= 60 ? ['Fully charged', ''] : a >= 25 ? ['Running steady', ''] : ['Low power', 'low'];
};
const scene = () => sceneSVG(S.equip.base, S.equip, mood()[1]);
const radio = (name, val, cur, inner, label) =>
  `<label><input type="radio" name="${name}" value="${val}"${String(cur) === String(val) ? ' checked' : ''}${label ? ` aria-label="${label}"` : ''}>${inner}</label>`;
const navHTML = (label, nextOff) => `<div class="nav">
  <button class="icb" data-act="nav" data-n="-1" aria-label="Previous">${ICON.prev}</button>
  <span class="nav-label">${label}</span>
  <button class="icb" data-act="nav" data-n="1" aria-label="Next"${nextOff ? ' disabled' : ''}>${ICON.next}</button></div>`;
const shiftMonth = (ym, n) => iso(new Date(+ym.slice(0, 4), +ym.slice(5) - 1 + n, 1)).slice(0, 7);

// The fastener mark: one hex nut per slot of a habit's day or week.
const hexSVG = s => `<svg viewBox="0 0 40 40" aria-hidden="true"><path class="hx" d="M20 3.5 34.3 11.75v16.5L20 36.5 5.7 28.25v-16.5z"/>${
  s === 'fail' ? '<path class="hm" d="M14 14l12 12M26 14 14 26"/>' : s === 'skip' ? '<path class="hm" d="M13.5 20h13"/>' : '<circle class="ho" cx="20" cy="20" r="6"/>'}</svg>`;
function slots(h, date, live) {
  const ms = periodMarks(S, h, date);
  return Array.from({ length: h.target }, (_, i) => {
    const x = ms[i], s = x ? x.m.s : '', own = x && x.d === date;
    const cls = `hex ${s}${x && !own ? ' other' : ''}${ui.fresh === h.id && i === ms.length - 1 ? ' fresh' : ''}`;
    const label = !x ? 'Open slot' : STATE[s] + (own ? '' : ', logged ' + fmt(x.d, { weekday: 'long' }));
    return live && own
      ? `<button class="${cls}" data-act="undo" data-id="${h.id}" aria-label="${label}, tap to undo">${hexSVG(s)}</button>`
      : `<span class="${cls}" role="img" aria-label="${label}">${hexSVG(s)}</span>`;
  }).join('');
}
const dot = s => {
  const p = dayProgress(S, s);
  return [p.done || p.fail ? `<i class="dot${p.fail ? ' fail' : ''}" style="--s:${(p.done / p.slots).toFixed(2)}"></i>` : '<i class="dot none"></i>',
    p.slots ? `, ${p.done} of ${p.slots} done${p.fail ? `, ${p.fail} failed` : ''}` : ''];
};
// cell(date) returns [html, label suffix] for one day of ui.month
function monthHTML(cell) {
  const [y, m] = ui.month.split('-').map(Number), t = today();
  const lead = (new Date(y, m - 1, 1).getDay() + 6) % 7, n = new Date(y, m, 0).getDate();
  let h = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => `<span class="dow">${d}</span>`).join('') + '<span></span>'.repeat(lead);
  for (let d = 1; d <= n; d++) {
    const s = `${ui.month}-${pad(d)}`, [inner, label] = cell(s);
    h += `<button class="day${s === t ? ' is-today' : ''}" data-act="day" data-date="${s}"${s > t ? ' disabled' : ''} aria-label="${fmt(s, { day: 'numeric', month: 'long' })}${label}"><span>${d}</span>${inner}</button>`;
  }
  return `<div class="month">${h}</div>`;
}

// ---------- screens ----------

function renderBase() {
  const t = today(), n = $('#botName');
  setHTML($('#baseScene'), scene());
  if (document.activeElement !== n) n.value = S.name;
  $('#mood').textContent = mood()[0];
  $('#baseTier').textContent = PART[S.equip.base].no + ' · ' + PART[S.equip.base].name;
  setHTML($('#baseToday'), S.habits.length
    ? `<ul class="mini">${S.habits.map(h => `<li><span>${esc(h.name)}</span><span class="h-slots">${slots(h, t, false)}</span></li>`).join('')}</ul>`
    : `<p class="note">No habits yet. Add one and ${esc(S.name)} starts earning bolts.</p>`);
  setHTML($('#attrs'), Object.entries(STATS).map(([k, d]) => `<li>
    <span class="a-ico">${ICON[k]}</span><span class="a-name">${d.name}<small>${d.gloss}</small></span>
    <span class="meter" style="--v:${S.stats[k]}%" role="meter" aria-label="${d.name}" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${S.stats[k]}"></span>
    <b>${S.stats[k]}</b></li>`).join(''));
  setHTML($('#week'), periodDates({ per: 'week' }, t).map(s => `<li${s === t ? ' class="is-today"' : ''} aria-label="${fmt(s, { weekday: 'long' })}${dot(s)[1]}">
    <span class="cell">${dot(s)[0]}</span>${fmt(s, { weekday: 'short' })}</li>`).join(''));
}

function renderShop() {
  const items = PARTS.filter(p => p.slot === ui.cat);
  const sel = PART[ui.sel]?.slot === ui.cat ? PART[ui.sel] : PART[S.equip[ui.cat]] || items[0];
  const tryOn = { ...S.equip, [ui.cat]: sel.id };
  const owned = S.owned.includes(sel.id), on = S.equip[ui.cat] === sel.id, w = WORDS[ui.cat] || WORDS.other;
  const need = ['bolts', 'nuts'].map(k => [(sel[k] || 0) - S[k], k]).filter(x => x[0] > 0).map(([n, k]) => `${n} more ${n === 1 ? k.slice(0, -1) : k}`).join(' and ');
  setHTML($('#shopScene'), sceneSVG(tryOn.base, tryOn, mood()[1], ui.cat === 'base' ? 'meet' : 'slice'));
  setHTML($('#shopDetail'), `<h2>${sel.name}</h2><p>${sel.no} · ${SLOTS[sel.slot]}</p><p class="cost">${owned ? 'Owned' : price(sel)}</p>${
    !owned ? (need ? `<button class="btn primary" disabled>Need ${need}</button>` : `<button class="btn primary" data-act="buy" data-id="${sel.id}">Buy</button>`)
      : !on ? `<button class="btn primary" data-act="equip" data-id="${sel.id}">${w[0]}</button>`
      : ui.cat === 'acc' ? '<button class="btn" data-act="unequip">Take off</button>'
      : `<button class="btn" disabled>${w[1]}</button>`}`);
  setHTML($('#shopCats'), Object.entries(SLOTS).map(([k, v]) => `<button data-act="cat" data-cat="${k}" aria-pressed="${k === ui.cat}">${v}</button>`).join(''));
  setHTML($('#shopGrid'), items.map(p => {
    const has = S.owned.includes(p.id), eq = S.equip[ui.cat] === p.id;
    return `<button class="tile${p.id === sel.id ? ' sel' : ''}${eq ? ' on' : ''}" data-act="pick" data-id="${p.id}" aria-pressed="${p.id === sel.id}">
      <span class="thumb">${partThumb(p)}</span><span class="t-name">${p.name}</span><span class="t-no">${p.no}</span>
      <span class="t-cost">${eq ? w[1] : has ? 'Owned' : price(p)}</span></button>`;
  }).join(''));
}

function habitRow(h, d) {
  const ms = periodMarks(S, h, d), off = ms.length >= h.target || d > today(), st = streak(S, h, d);
  return `<li class="habit">
    <button class="h-main" data-act="edit" data-id="${h.id}" aria-label="Edit ${esc(h.name)}">
      <span class="h-ico">${ICON[h.stat]}</span>
      <span class="h-name">${esc(h.name)}${h.kind === 'bad' ? '<i class="tag">break</i>' : ''}</span>
      <span class="h-meta">${h.target} per ${h.per} · ${STATS[h.stat].name} · ${plural(WEIGHTS[h.weight].bolts, 'bolt')}${st > 1 ? ` · ${st} ${h.per}s running` : ''}</span>
    </button>
    <div class="h-slots">${slots(h, d, true)}</div>
    <div class="h-acts">${['done', 'skip', 'fail'].map(s =>
      `<button class="btn ${s}" data-act="mark" data-id="${h.id}" data-s="${s}"${off ? ' disabled' : ''}>${BTN[h.kind][s]}</button>`).join('')}</div>
  </li>`;
}

function formHTML(h) {
  const v = h || { name: '', kind: 'good', per: 'day', target: 1, stat: 'battery', weight: 'medium' };
  return `<form class="form">
    <h2>${h ? 'Edit habit' : 'New habit'}</h2>
    <label class="field"><span>Name</span><input name="name" required maxlength="40" value="${esc(v.name)}" placeholder="Read 20 minutes" autocomplete="off"></label>
    <fieldset class="field"><legend>Type</legend>
      <div class="seg">${radio('kind', 'good', v.kind, '<span>Build it</span>')}${radio('kind', 'bad', v.kind, '<span>Break it</span>')}</div>
      <p class="hint">Build it: you earn each time you do it. Break it: you earn each time you resist it.</p></fieldset>
    <fieldset class="field"><legend>How often</legend>
      <div class="row"><div class="stepper">
        <button type="button" class="icb" data-act="step" data-n="-1" aria-label="Fewer">${ICON.minus}</button>
        <input name="target" type="number" min="1" max="7" value="${v.target}" aria-label="Times">
        <button type="button" class="icb" data-act="step" data-n="1" aria-label="More">${ICON.plus}</button></div>
        <span>times per</span>
        <div class="seg">${radio('per', 'day', v.per, '<span>Day</span>')}${radio('per', 'week', v.per, '<span>Week</span>')}</div></div></fieldset>
    <fieldset class="field"><legend>Attribute it trains</legend>
      <div class="seg wide">${Object.entries(STATS).map(([k, d]) => radio('stat', k, v.stat, `<span>${ICON[k]}${d.name}<small>${d.gloss}</small></span>`)).join('')}</div></fieldset>
    <fieldset class="field"><legend>Weight</legend>
      <div class="seg wide">${Object.entries(WEIGHTS).map(([k, w]) => radio('weight', k, v.weight, `<span>${w.name}<small>${plural(w.bolts, 'bolt')}, ${plural(w.pts, 'point')}</small></span>`)).join('')}</div>
      <p class="hint">Done pays this much. Failed takes the same amount back.</p></fieldset>
    <div class="row">${h ? '<button type="button" class="btn danger" data-act="del">Delete habit</button>' : ''}<span class="grow"></span>
      <button type="button" class="btn" data-act="cancel">Cancel</button><button class="btn primary">${h ? 'Save changes' : 'Add habit'}</button></div>
  </form>`;
}

function renderHabits() {
  const t = today(), d = ui.date, p = dayProgress(S, d);
  setHTML($('#habScene'), scene());
  $('#habDay').textContent = d === t ? 'Today' : fmt(d, { weekday: 'long' });
  $('#habDate').textContent = fmt(d, { day: 'numeric', month: 'long' });
  $('#habSum').textContent = !S.habits.length ? 'No habits yet' : `${p.done} of ${p.slots} fastened` + (p.fail ? `, ${p.fail} failed` : '');
  if (ui.form) { setHTML($('#habBar'), ''); setHTML($('#habBody'), formHTML(habit(ui.form.id))); return; }
  const cal = ui.view === 'cal';
  setHTML($('#habBar'), `${cal ? navHTML(fmt(ui.month + '-01', { month: 'long', year: 'numeric' }), ui.month >= t.slice(0, 7))
      : navHTML(fmt(d, { weekday: 'short', day: 'numeric', month: 'short' }), d >= t)}
    ${!cal && d !== t ? '<button class="btn" data-act="today">Back to today</button>' : ''}<span class="grow"></span>
    <div class="seg"><button data-act="view" data-v="day" aria-pressed="${!cal}">Day</button><button data-act="view" data-v="cal" aria-pressed="${cal}">Calendar</button></div>
    <button class="btn primary" data-act="new">${ICON.plus}Add habit</button>`);
  setHTML($('#habBody'), cal ? monthHTML(dot)
    : S.habits.length ? `<ul class="list">${S.habits.map(h => habitRow(h, d)).join('')}</ul>`
    : `<div class="empty"><h2>Nothing to fasten yet</h2>
        <p>A habit is something you want to do, or stop doing, a set number of times per day or week. Each one feeds ${esc(S.name)}.</p>
        <button class="btn primary" data-act="new">${ICON.plus}Add your first habit</button>
        <p>Or start from one of these:</p>
        <div class="chips">${SUGGEST.map((s, i) => `<button class="btn" data-act="suggest" data-i="${i}">${s.name} · ${s.target} per ${s.per}</button>`).join('')}</div></div>`);
}

function renderDiary() {
  const ed = !!ui.page, t = today();
  $('#diaryBay').hidden = $('#diaryPane').hidden = ed;
  $('#editor').hidden = !ed;
  if (ed) return renderTools();
  const n = Object.keys(S.diary).filter(d => d.startsWith(ui.month)).length, label = fmt(ui.month + '-01', { month: 'long', year: 'numeric' });
  setHTML($('#diaryScene'), scene());
  $('#diaryMonth').textContent = 'Diary';
  $('#diarySum').textContent = n ? `${plural(n, 'page')} written in ${fmt(ui.month + '-01', { month: 'long' })}` : `No pages written in ${fmt(ui.month + '-01', { month: 'long' })}`;
  $('#writeToday').textContent = S.diary[t] ? "Open today's page" : "Write today's page";
  setHTML($('#diaryBar'), navHTML(label, ui.month >= t.slice(0, 7)));
  setHTML($('#diaryBody'), monthHTML(s => S.diary[s] ? [`<i class="inked">${ICON.diary}</i>`, ', has a page'] : ['', '']));
}

// The printed diary page: dated header, a charge gauge to fill in, ruled lines with an annotation margin.
function pageTemplate(date) {
  const d = day(date), doy = Math.round((d - new Date(d.getFullYear(), 0, 1, 12)) / 864e5) + 1;
  const orbits = (cx, cy, rx, ry) => [0, 60, 120].map(a => `<ellipse cx="${cx}" cy="${cy}" rx="${rx}" ry="${ry}" transform="rotate(${a} ${cx} ${cy})"/>`).join('');
  return `<svg viewBox="0 0 ${PAGE_W} ${PAGE_H}" aria-hidden="true">
    <g class="t-art">${orbits(1290, 640, 250, 88)}<circle cx="1290" cy="640" r="10"/></g>
    <path class="t-rule" d="${Array.from({ length: 13 }, (_, i) => `M90 ${220 + i * 55}h1420`).join('')}"/>
    <path class="t-margin" d="M170 182v716"/>
    <path class="t-headrule" d="M90 160h1420"/>
    <text class="t-day" x="88" y="100">${fmt(date, { weekday: 'long' })}</text>
    <text class="t-date" x="90" y="138">${fmt(date, { day: 'numeric', month: 'long', year: 'numeric' })}</text>
    <text class="t-label" x="1014" y="58">CHARGE</text>
    <g class="t-hex">${Array.from({ length: 5 }, (_, i) => `<path transform="translate(${1008 + i * 46} 68)" d="M20 3.5 34.3 11.75v16.5L20 36.5 5.7 28.25v-16.5z"/>`).join('')}</g>
    <text class="t-label" x="1392" y="96" text-anchor="end">DAY ${doy}</text>
    <g class="t-atom">${orbits(1462, 90, 44, 15)}<circle cx="1462" cy="90" r="5"/></g>
    <text class="t-label" x="90" y="922">${esc(S.name).toUpperCase()}'S LOG</text>
    <text class="t-label" x="1510" y="922" text-anchor="end">NB-D${date.replaceAll('-', '')}</text>
  </svg>`;
}

function renderTools() {
  const p = S.pen;
  const sw = (name, val, cur, label) => radio(name, val, cur, `<span class="sw${name === 'paper' ? ' sq' : ''}" style="--c:${name === 'paper' ? PAPERS[val][1] : val}"></span>`, label);
  $('#page').className = 'page ' + ui.paper;
  setHTML($('#tpl'), pageTemplate(ui.page));
  setHTML($('#tools'), `
    <button class="btn" data-act="back">${ICON.prev}Calendar</button>
    <h2>${fmt(ui.page, { weekday: 'long', day: 'numeric', month: 'long' })}</h2><span class="grow"></span>
    <div class="stab"><span id="paperL">Paper</span><div class="swatches" role="radiogroup" aria-labelledby="paperL">${Object.entries(PAPERS).map(([k, [n]]) => sw('paper', k, ui.paper, n)).join('')}</div></div>
    <div class="swatches" role="radiogroup" aria-label="Ink colour">${PEN_COLORS.map(([n, c]) => sw('color', c, ui.erase ? '' : p.color, n)).join('')}</div>
    <div class="seg sizes" role="radiogroup" aria-label="Pen size">${PEN_SIZES.map(([n], i) => radio('size', i, p.size, `<span><i style="--d:${i}"></i></span>`, n)).join('')}</div>
    <button class="icb" data-act="erase" aria-pressed="${ui.erase}" aria-label="Eraser">${ICON.eraser}</button>
    <div class="stab"><span id="stabL">Stabilizer</span><div class="seg" role="radiogroup" aria-labelledby="stabL">${STAB.map(([n], i) => radio('stab', i, p.stab, `<span>${n}</span>`)).join('')}</div></div>
    <button class="icb" data-act="pundo" aria-label="Undo stroke">${ICON.undo}</button>
    <button class="icb" data-act="predo" aria-label="Redo stroke">${ICON.redo}</button>
    <button class="icb" data-act="pclear" aria-label="Clear page">${ICON.trash}</button>`);
  Object.assign(ink.tool, { color: p.color, size: p.size, stab: p.stab, erase: ui.erase });
}

let reactT;
function render() {
  const t = today();
  $('#today').textContent = fmt(t, { weekday: 'long', day: 'numeric', month: 'long' });
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

// ---------- diary page ----------

const ink = Ink($('#ink'), savePage);
function savePage() {
  const date = ui.page, st = ink.strokes;
  idb('readwrite', s => st.length ? s.put({ paper: ui.paper, strokes: st }, date) : s.delete(date)).catch(() => toast('This page could not be saved. Free up storage and try again.'));
  st.length ? S.diary[date] = 1 : delete S.diary[date];
  save();
}
function openPage(date) {
  const c = $('#ink');
  ui.tab = 'diary'; ui.page = date; ui.erase = false; ui.paper = S.pen.paper;
  // no drawing until the saved page is on screen, or a slow load would be overwritten by a blank one
  c.style.pointerEvents = 'none';
  ink.load([]);
  idb('readonly', s => s.get(date)).then(pg => {
    if (ui.page !== date) return;
    if (Array.isArray(pg)) pg = { paper: 'dark', strokes: pg }; // pages saved before paper was a choice
    if (pg) ui.paper = pg.paper;
    ink.load(pg ? pg.strokes : []);
    c.style.pointerEvents = '';
    render();
  })
    .catch(() => { ui.page = null; render(); toast('Could not open this page.'); });
}
new ResizeObserver(() => {
  const wrap = $('#pageWrap'), w = Math.min(wrap.clientWidth, wrap.clientHeight * PAGE_W / PAGE_H), h = w * PAGE_H / PAGE_W, p = $('#page');
  if (!w) return;
  p.style.width = w + 'px'; p.style.height = h + 'px';
  ink.resize(w, h);
}).observe($('#pageWrap'));

// ---------- actions ----------

const ACT = {
  tab: d => { ui.tab = d.tab; ui.form = null; },
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
  suggest: d => { S.habits.push({ ...SUGGEST[d.i], id: newId() }); },
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
  back: () => { ui.page = null; },
  erase: () => { ui.erase = !ui.erase; },
  pundo: () => ink.undo(),
  predo: () => ink.redo(),
  pclear: () => { if (ink.strokes.length && confirm('Clear everything on this page?')) ink.clear(); },
};

document.addEventListener('click', e => {
  const b = e.target.closest('[data-act]');
  if (!b || b.disabled) return;
  ACT[b.dataset.act](b.dataset, b);
  save(); render();
});
document.addEventListener('submit', e => {
  e.preventDefault();
  const f = Object.fromEntries(new FormData(e.target)), id = ui.form.id || newId();
  const h = { id, name: f.name.trim(), kind: f.kind, per: f.per, target: Math.min(7, Math.max(1, +f.target || 1)), stat: f.stat, weight: f.weight };
  if (!h.name) return;
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
  if (t.name === 'paper') {
    ui.paper = t.value;
    // keep the ink readable: each paper's default ink is the other paper's colour
    const ice = PEN_COLORS[0][1], black = PEN_COLORS[4][1], cream = t.value === 'cream';
    if (S.pen.color === (cream ? ice : black)) S.pen.color = cream ? black : ice;
    if (ink.strokes.length) savePage();
  }
  save(); render();
});
$('#botName').addEventListener('input', e => { S.name = e.target.value.trim() || 'Rivet'; save(); });
document.addEventListener('visibilitychange', () => { if (!document.hidden) render(); }); // the date may have rolled over

navigator.storage?.persist?.(); // ask the browser not to evict the diary under storage pressure
if ('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js').catch(() => {});
render();
