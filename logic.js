'use strict';
// Habit rules. No DOM in here so `node test.js` can check it.

const STATS = {
  battery: { name: 'Battery', gloss: 'energy' },
  processor: { name: 'Processor', gloss: 'mind' },
  chassis: { name: 'Chassis', gloss: 'body' },
  core: { name: 'Core', gloss: 'heart' },
};
const WEIGHTS = {
  light: { name: 'Light', bolts: 5, pts: 1 },
  medium: { name: 'Medium', bolts: 10, pts: 2 },
  heavy: { name: 'Heavy', bolts: 20, pts: 4 },
};

const pad = n => String(n).padStart(2, '0');
const iso = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const day = s => new Date(s + 'T12:00'); // noon keeps day steps clear of DST edges
const addDays = (s, n) => { const d = day(s); d.setDate(d.getDate() + n); return iso(d); };
const weekStart = s => addDays(s, -((day(s).getDay() + 6) % 7)); // Monday
const periodDates = (h, s) => h.per === 'week' ? Array.from({ length: 7 }, (_, i) => addDays(weekStart(s), i)) : [s];
const periodMarks = (S, h, s) => periodDates(h, s).flatMap(d => (S.marks[d]?.[h.id] || []).map(m => ({ d, m })));
const dow = s => (day(s).getDay() + 6) % 7; // 0 is Monday
// a habit runs from its start day to its end day, on its weekdays; older habits without these always run
const active = (h, s) => (!h.start || h.start <= s) && (!h.end || s <= h.end) && (!h.days || h.days.includes(dow(s)));
const locked = (S, h, s) => periodMarks(S, h, s).filter(x => x.m.s === 'done').length >= h.target;

function streak(S, h, s) {
  const step = h.per === 'week' ? 7 : 1;
  let n = 0;
  for (let d = s, i = 0; i < 2000; d = addDays(d, -step), i++) {
    if (h.start && d < h.start) break;
    if (!periodDates(h, d).some(x => active(h, x))) continue; // a day off neither breaks nor extends the streak
    if (!locked(S, h, d)) break;
    n++;
  }
  return n;
}

// A habit's lifetime up to day t: slots it asked for, how they went, and its longest run of full periods.
// The period holding t is still open, so missing it does not end the run.
function record(S, h, t) {
  const first = h.start || Object.keys(S.marks).sort().find(d => S.marks[d][h.id]) || t, last = h.end && h.end < t ? h.end : t;
  const r = { slots: 0, done: 0, fail: 0, best: 0, now: streak(S, h, t) };
  let run = 0;
  for (let d = h.per === 'week' ? weekStart(first) : first; d <= last; d = addDays(d, h.per === 'week' ? 7 : 1)) {
    if (!periodDates(h, d).some(x => active(h, x))) continue;
    const ms = periodMarks(S, h, d);
    r.slots += h.target;
    r.done += ms.filter(x => x.m.s === 'done').length;
    r.fail += ms.filter(x => x.m.s === 'fail').length;
    if (locked(S, h, d)) r.best = Math.max(r.best, ++run);
    else if (!periodDates(h, d).includes(t)) run = 0;
  }
  return r;
}

// Fills the next slot of the habit's period. Each mark stores exactly what it changed, so undo is exact.
function mark(S, h, s, kind) {
  if (periodMarks(S, h, s).length >= h.target) return null;
  const w = WEIGHTS[h.weight], m = { s: kind, b: 0, p: 0, n: 0 };
  if (kind === 'done') { m.b = w.bolts; m.p = Math.min(w.pts, 100 - S.stats[h.stat]); }
  if (kind === 'fail') { m.b = -Math.min(w.bolts, S.bolts); m.p = -Math.min(w.pts, S.stats[h.stat]); }
  ((S.marks[s] ??= {})[h.id] ??= []).push(m);
  // a nut for every finished week, or every third finished day in a row
  if (kind === 'done' && locked(S, h, s) && (h.per === 'week' || streak(S, h, s) % 3 === 0)) m.n = 1;
  S.bolts += m.b; S.nuts += m.n; S.stats[h.stat] += m.p;
  return m;
}

function undo(S, h, s) {
  const arr = S.marks[s]?.[h.id];
  if (!arr?.length) return null;
  const m = arr.pop();
  let n = m.n;
  if (!locked(S, h, s)) for (const x of periodMarks(S, h, s)) { n += x.m.n; x.m.n = 0; }
  S.bolts = Math.max(0, S.bolts - m.b);
  S.nuts = Math.max(0, S.nuts - n);
  S.stats[h.stat] = Math.min(100, Math.max(0, S.stats[h.stat] - m.p));
  if (!arr.length) { delete S.marks[s][h.id]; if (!Object.keys(S.marks[s]).length) delete S.marks[s]; }
  return m;
}

function dayProgress(S, s) {
  let done = 0, fail = 0, slots = 0;
  for (const h of S.habits) {
    const ms = S.marks[s]?.[h.id] || [];
    done += ms.filter(m => m.s === 'done').length;
    fail += ms.filter(m => m.s === 'fail').length;
    slots += h.per === 'day' && active(h, s) ? h.target : ms.length;
  }
  return { done, fail, slots };
}

if (typeof module !== 'undefined') module.exports = { STATS, WEIGHTS, iso, addDays, weekStart, periodMarks, active, locked, streak, record, mark, undo, dayProgress };
