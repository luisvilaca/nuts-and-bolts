'use strict';
// Shared by every screen: helpers, icons, the saved state, and the pieces screens are built from.
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
    // month sheet with binder posts; today is a joint
    calendar: i('<path d="M4 6h16v14H4zM4 10.500h16M8.500 3.500v4.500M15.500 3.500v4.500M7.500 14h3M7.500 17h3"/><circle cx="15.500" cy="15.500" r="1.700"/>'),
    // bent arms with a shoulder joint
    undo: i('<path d="M9 5.500 5 9.500l4 4M5 9.500h9.500l4 4V16"/><circle cx="18.500" cy="17.800" r="1.600"/>'),
    redo: i('<path d="M15 5.500l4 4-4 4M19 9.500H9.500l-4 4V16"/><circle cx="5.500" cy="17.800" r="1.600"/>'),
    // wiper head on an arm
    eraser: i('<path d="M4.500 14.500 11 8l5.500 5.500L11 19H9zM7.750 11.250l5.500 5.500M14 10.500l3.800-3.800M4 21.500h16"/><circle cx="18.900" cy="5.600" r="1.500"/>'),
    // a bin with eyes and an antenna
    trash: i('<path d="M6.500 9h11l-1 11.500h-9zM4.500 9h15M12 9V5.500M10 13v3M14 13v3"/><circle cx="12" cy="4.300" r="1.200"/>'),
  };
})();

const DOW = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
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
  remind: { on: false, time: '21:00' }, backedUp: null,
});
// Brings saved state, from this tablet or a backup file, up to the current shape.
function upgrade(o) {
  const s = Object.assign(FRESH(), o);
  s.pen = { ...FRESH().pen, ...s.pen };
  s.remind = { ...FRESH().remind, ...s.remind };
  if (!PEN_COLORS.some(c => c[1] === s.pen.color)) s.pen.color = PEN_COLORS[0][1]; // a colour from an older palette
  if (typeof s.pen.paper !== 'number') s.pen.paper = s.pen.paper === 'cream' ? 1 : 0; // was a paper name
  return s;
}
function load() {
  const raw = localStorage.getItem(KEY);
  if (!raw) return FRESH();
  try { return upgrade(JSON.parse(raw)); }
  catch { localStorage.setItem(KEY + '-unreadable', raw); return FRESH(); } // keep the bad copy instead of overwriting it
}
function save() {
  try { localStorage.setItem(KEY, JSON.stringify(S)); }
  catch { toast('Could not save. The tablet is out of storage for this app.'); }
  syncReminder();
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
function monthHTML(cell, ahead) {
  const [y, m] = ui.month.split('-').map(Number), t = today();
  const lead = (new Date(y, m - 1, 1).getDay() + 6) % 7, n = new Date(y, m, 0).getDate();
  let h = DOW.map(d => `<span class="dow">${d}</span>`).join('') + '<span></span>'.repeat(lead);
  for (let d = 1; d <= n; d++) {
    const s = `${ui.month}-${pad(d)}`, [inner, label] = cell(s);
    h += `<button class="day${s === t ? ' is-today' : ''}" data-act="day" data-date="${s}"${s > t && !ahead ? ' disabled' : ''} aria-label="${fmt(s, { day: 'numeric', month: 'long' })}${label}"><span>${d}</span>${inner}</button>`;
  }
  return `<div class="month">${h}</div>`;
}
