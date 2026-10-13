'use strict';
const $ = s => document.querySelector(s);
const $$ = s => document.querySelectorAll(s);
const esc = s => String(s).replace(/[&<>"']/g, c => `&#${c.charCodeAt(0)};`);
// skips untouched regions so running animations and half-filled forms survive a render
const setHTML = (el, html) => { if (el._h !== html) { el.innerHTML = html; el._h = html; } };
const today = () => iso(new Date());
const fmt = (s, o) => day(s).toLocaleDateString('en-GB', o);
const plural = (n, w) => `${n} ${w}${n === 1 ? '' : 's'}`;

// Icons share one robot grammar: angular strokes, and a pivot joint (a small circle) wherever a part would hinge.
const ICON = (() => {
  const i = d => `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true">${d}</svg>`;
  const hex = '<path d="M12 2.8l8 4.6v9.2l-8 4.6-8-4.6V7.4z"/>';
  return {
    bolts: i('<circle cx="12" cy="12" r="8.5"/><path d="M8 12h8M12 8v8"/>'),
    nuts: i(hex + '<circle cx="12" cy="12" r="3.6"/>'),
    battery: i('<path d="M3 8h15v8H3zM20.5 10.5v3M6.5 11v2M10 11v2"/>'),
    processor: i('<path d="M7 7h10v10H7zM10 3.5V7M14 3.5V7M10 17v3.5M14 17v3.5M3.5 10H7M3.5 14H7M17 10h3.5M17 14h3.5"/><circle cx="12" cy="12" r="1.6"/>'),
    chassis: i('<path d="M7 4h10l2.5 3.5v7L17 20H7l-2.5-5.5v-7zM9 15.5h6"/><circle cx="12" cy="10" r="2"/>'),
    core: i('<path d="M12 20l-7.5-7.5V8L8 4.5h2L12 7l2-2.5h2L19.5 8v4.5z"/><circle cx="12" cy="11.5" r="1.7"/>'),
    // robot head
    base: i('<path d="M7 8.5h10l2 2v7l-2 2H7l-2-2v-7zM12 8.5v-3M9.5 13v2M14.5 13v2M2.8 12.5v3M21.2 12.5v3"/><circle cx="12" cy="4.2" r="1.3"/>'),
    // gripper claw closing on a nut
    shop: i('<path d="M12 2.5v3.6M10.8 8.4 6.5 11.5V16L9 18.5M13.2 8.4l4.3 3.1V16L15 18.5M12 11.6l2.3 1.3v2.7L12 16.9l-2.3-1.3v-2.7z"/><circle cx="12" cy="7.5" r="1.4"/>'),
    habits: i(hex + '<path d="M8.5 12.2l2.4 2.4 4.6-5"/>'),
    // pen on a jointed arm
    diary: i('<path d="M15.5 6 18 8.5l-9 9-4.5 2 2-4.5zM6.5 15 9 17.5M13 8.500l2.500 2.500M17.200 6.800l1.300-1.300"/><circle cx="19.600" cy="4.400" r="1.500"/>'),
    // sheet printed with circuit traces
    paper: i('<path d="M6 3h9l4 4v14H6zM15 3v4h4M9 11h4.400M9 14.500h2.200M9 18h4.400"/><circle cx="14.700" cy="11" r="1.100"/><circle cx="12.500" cy="14.500" r="1.100"/><circle cx="14.700" cy="18" r="1.100"/>'),
    prev: i('<path d="M14.500 5.500 9.700 10.700M9.700 13.300l4.800 5.200"/><circle cx="8.600" cy="12" r="1.600"/>'),
    next: i('<path d="M9.500 5.500l4.800 5.200M14.300 13.300l-4.800 5.200"/><circle cx="15.400" cy="12" r="1.600"/>'),
    plus: i('<path d="M12 5v4.500M12 14.500V19M5 12h4.500M14.500 12H19"/><circle cx="12" cy="12" r="1.500"/>'),
    minus: i('<path d="M5 12h14"/>'),
    // bent arms with a shoulder joint
    undo: i('<path d="M9 5.500 5 9.500l4 4M5 9.500h9.500l4 4V16"/><circle cx="18.500" cy="17.800" r="1.600"/>'),
    redo: i('<path d="M15 5.500l4 4-4 4M19 9.500H9.500l-4 4V16"/><circle cx="5.500" cy="17.800" r="1.600"/>'),
    // wiper head on an arm
    eraser: i('<path d="M4.500 14.500 11 8l5.500 5.500L11 19H9zM7.750 11.250l5.500 5.500M14 10.500l3.800-3.800M4 21.500h16"/><circle cx="18.900" cy="5.600" r="1.500"/>'),
    // a bin with eyes and an antenna
    trash: i('<path d="M6.500 9h11l-1 11.500h-9zM4.500 9h15M12 9V5.500M10 13v3M14 13v3"/><circle cx="12" cy="4.300" r="1.200"/>'),
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
  papers: [...PAPERS],
  pen: { color: PEN_COLORS[0][1], size: 1, stab: 1, eraser: 1, paper: 0, seen: false }, // paper is a slot in papers
});
function load() {
  const raw = localStorage.getItem(KEY);
  if (!raw) return FRESH();
  try {
    const s = Object.assign(FRESH(), JSON.parse(raw));
    s.pen = { ...FRESH().pen, ...s.pen };
    if (!PEN_COLORS.some(c => c[1] === s.pen.color)) s.pen.color = PEN_COLORS[0][1]; // a colour from an older palette
    if (typeof s.pen.paper !== 'number') s.pen.paper = s.pen.paper === 'cream' ? 1 : 0; // was a paper name
    return s;
  }
  catch { localStorage.setItem(KEY + '-unreadable', raw); return FRESH(); } // keep the bad copy instead of overwriting it
}
function save() {
  try { localStorage.setItem(KEY, JSON.stringify(S)); }
  catch { toast('Could not save. The tablet is out of storage for this app.'); }
}
let S = load();
const ui = { tab: 'base', date: today(), view: 'day', month: today().slice(0, 7), cat: 'head', sel: null, form: null, page: null, paper: PAPERS[0], pop: null, erase: false, fresh: null, react: '' };
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
    <g class="t-art">${orbits(1290, PAGE_H - 330, 250, 88)}<circle cx="1290" cy="${PAGE_H - 330}" r="10"/></g>
    <path class="t-rule" d="${Array.from({ length: 37 }, (_, i) => `M90 ${220 + i * 55}h1420`).join('')}"/>
    <path class="t-margin" d="M170 182v${PAGE_H - 222}"/>
    <path class="t-headrule" d="M90 160h1420"/>
    <text class="t-day" x="88" y="100">${fmt(date, { weekday: 'long' })}</text>
    <text class="t-date" x="90" y="138">${fmt(date, { day: 'numeric', month: 'long', year: 'numeric' })}</text>
    <text class="t-label" x="1014" y="58">CHARGE</text>
    <g class="t-hex">${Array.from({ length: 5 }, (_, i) => `<path transform="translate(${1008 + i * 46} 68)" d="M20 3.5 34.3 11.75v16.5L20 36.5 5.7 28.25v-16.5z"/>`).join('')}</g>
    <text class="t-label" x="1392" y="96" text-anchor="end">DAY ${doy}</text>
    <g class="t-atom">${orbits(1462, 90, 44, 15)}<circle cx="1462" cy="90" r="5"/></g>
    <text class="t-label" x="90" y="${PAGE_H - 18}">${esc(S.name).toUpperCase()}'S LOG</text>
    <text class="t-label" x="1510" y="${PAGE_H - 18}" text-anchor="end">NB-D${date.replaceAll('-', '')}</text>
  </svg>`;
}

// The printed template picks light or dark ink for itself from the paper's brightness.
const lightPaper = hex => { const n = parseInt(hex.slice(1), 16); return 0.299 * (n >> 16) + 0.587 * (n >> 8 & 255) + 0.114 * (n & 255) > 140; };
function applyPaper() {
  const p = $('#page');
  p.className = 'page ' + (lightPaper(ui.paper) ? 'light' : 'dark');
  p.style.setProperty('--paper', ui.paper);
}
function setPaper(hex) {
  ui.paper = hex;
  // keep the ink readable: light paper takes the black ink, dark paper the ice blue
  const ice = PEN_COLORS[0][1], black = PEN_COLORS[4][1], light = lightPaper(hex);
  if (S.pen.color === (light ? ice : black)) S.pen.color = light ? black : ice;
  if (ink.strokes.length) savePage();
}

// One line: calendar, date, paper, pen and inks, eraser, then undo, redo and bin. Settings live in popovers.
function renderTools() {
  const p = S.pen, same = (a, b) => a.toLowerCase() === b.toLowerCase();
  const sw = (name, val, on, c, label, slot = '') => `<label><input type="radio" name="${name}" value="${val}"${on ? ' checked' : ''} aria-label="${label}"><span class="sw${slot && ' sq'}" ${slot} style="--c:${c}"></span></label>`;
  const pop = (id, icon, label, body, attrs = `data-act="pop" data-pop="${id}"`) => `<div class="pw">
    <button class="icb" ${attrs} aria-expanded="${ui.pop === id}" aria-label="${label}">${icon}</button>
    ${ui.pop === id ? `<div class="pop" role="group" aria-label="${label}">${body}</div>` : ''}</div>`;
  const seg = (name, list, cur) => `<div class="seg">${list.map(([n], i) => radio(name, i, cur, `<span>${n}</span>`)).join('')}</div>`;
  applyPaper();
  setHTML($('#tpl'), pageTemplate(ui.page));
  setHTML($('#tools'), `
    <button class="btn" data-act="back">${ICON.prev}Calendar</button>
    <h2>${fmt(ui.page, { weekday: 'short', day: 'numeric', month: 'short' })}</h2><span class="grow"></span>
    ${pop('paper', ICON.paper, 'Paper colours', `<h3>Paper colours</h3>${S.papers.map((c, i) => `<label class="hexrow"><span class="sw sq" data-slot="${i}" style="--c:${c}"></span>
      <input data-hex="${i}" value="${c}" maxlength="7" spellcheck="false" autocomplete="off" autocapitalize="characters" aria-label="Paper ${i + 1}, hex colour"></label>`).join('')}
      <button class="btn" data-act="paperReset">Restore the originals</button>`)}
    <div class="swatches" role="radiogroup" aria-label="Paper">${S.papers.map((c, i) => sw('paper', i, same(c, ui.paper), c, 'Paper ' + (i + 1), `data-slot="${i}"`)).join('')}</div>
    <i class="sep"></i>
    ${pop('pen', ICON.diary, 'Pen settings', `<h3>Nib</h3>${seg('size', PEN_SIZES, p.size)}<h3>Stabilizer</h3>${seg('stab', STAB, p.stab)}`)}
    <div class="swatches" role="radiogroup" aria-label="Ink colour">${PEN_COLORS.map(([n, c]) => sw('color', c, !ui.erase && c === p.color, c, n)).join('')}</div>
    <i class="sep"></i>
    ${pop('eraser', ICON.eraser, 'Eraser. Hold for width', `<h3>Eraser width</h3>${seg('eraser', ERASER_SIZES, p.eraser)}`, `data-act="erase" data-hold="eraser" aria-pressed="${ui.erase}"`)}
    <i class="sep"></i>
    <button class="icb" data-act="pundo" aria-label="Undo stroke">${ICON.undo}</button>
    <button class="icb" data-act="predo" aria-label="Redo stroke">${ICON.redo}</button>
    <button class="icb" data-act="pclear" aria-label="Bin this page">${ICON.trash}</button>`);
  Object.assign(ink.tool, { color: p.color, size: p.size, stab: p.stab, eraser: p.eraser, erase: ui.erase });
}

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

// ---------- diary page ----------

const ink = Ink($('#ink'), $('#pageWrap'), savePage);
ink.pen = !!S.pen.seen;
function savePage() {
  const date = ui.page, st = ink.strokes;
  // the page keeps its own paper colour, so replacing a slot later does not repaint old entries
  idb('readwrite', s => st.length ? s.put({ paper: ui.paper, strokes: st }, date) : s.delete(date)).catch(() => toast('This page could not be saved. Free up storage and try again.'));
  st.length ? S.diary[date] = 1 : delete S.diary[date];
  S.pen.seen = ink.pen;
  save();
}
function openPage(date) {
  const c = $('#ink');
  ui.tab = 'diary'; ui.page = date; ui.erase = false; ui.pop = null; ui.paper = S.papers[S.pen.paper] || PAPERS[0];
  $('#pageWrap').scrollTop = 0;
  // no drawing until the saved page is on screen, or a slow load would be overwritten by a blank one
  c.style.pointerEvents = 'none';
  ink.load([]);
  idb('readonly', s => s.get(date)).then(pg => {
    if (ui.page !== date) return;
    if (Array.isArray(pg)) pg = { paper: 'dark', strokes: pg }; // pages saved before paper was a choice
    if (pg) ui.paper = { dark: PAPERS[0], cream: PAPERS[1] }[pg.paper] || pg.paper; // older pages stored a paper name, not a colour
    ink.load(pg ? pg.strokes : []);
    c.style.pointerEvents = '';
    render();
  })
    .catch(() => { ui.page = null; render(); toast('Could not open this page.'); });
}
new ResizeObserver(() => { const p = $('#page'); if (p.clientWidth) ink.resize(p.clientWidth, p.clientHeight); }).observe($('#page'));

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
  back: () => { ui.page = null; ui.pop = null; },
  pop: d => { ui.pop = ui.pop === d.pop ? null : d.pop; },
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
