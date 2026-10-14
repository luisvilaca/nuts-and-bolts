// node test.js
const assert = require('assert');
const { mark, undo, streak, locked, weekStart, dayProgress, active, record } = require('./logic.js');

const fresh = () => ({ bolts: 0, nuts: 0, stats: { battery: 20, processor: 20, chassis: 20, core: 20 }, habits: [], marks: {} });
const daily = { id: 'a', per: 'day', target: 1, stat: 'battery', weight: 'medium' };
const weekly = { id: 'b', per: 'week', target: 2, stat: 'chassis', weight: 'heavy' };

let S = fresh();
S.habits = [daily, weekly];

// done pays, a full period refuses more marks
assert.deepStrictEqual(mark(S, daily, '2026-10-12', 'done'), { s: 'done', b: 10, p: 2, n: 0 });
assert.strictEqual(mark(S, daily, '2026-10-12', 'done'), null);
assert.strictEqual(S.bolts, 10);
assert.strictEqual(S.stats.battery, 22);

// third day in a row earns a nut, undo takes it back
mark(S, daily, '2026-10-13', 'done');
assert.strictEqual(mark(S, daily, '2026-10-14', 'done').n, 1);
assert.strictEqual(streak(S, daily, '2026-10-14'), 3);
assert.strictEqual(S.nuts, 1);
undo(S, daily, '2026-10-14');
assert.deepStrictEqual([S.nuts, S.bolts, S.stats.battery], [0, 20, 24]);
assert.strictEqual(locked(S, daily, '2026-10-14'), false);

// failing never drives a balance below zero, and undo restores exactly what was taken
S = fresh(); S.habits = [daily, weekly]; S.bolts = 4;
assert.deepStrictEqual(mark(S, daily, '2026-10-12', 'fail'), { s: 'fail', b: -4, p: -2, n: 0 });
assert.strictEqual(S.bolts, 0);
undo(S, daily, '2026-10-12');
assert.deepStrictEqual([S.bolts, S.stats.battery, S.marks], [4, 20, {}]);

// weekly slots span Monday to Sunday; finishing the week earns a nut
assert.strictEqual(weekStart('2026-10-18'), '2026-10-12');
mark(S, weekly, '2026-10-12', 'done');
assert.strictEqual(mark(S, weekly, '2026-10-15', 'done').n, 1);
assert.strictEqual(mark(S, weekly, '2026-10-17', 'done'), null);
// undoing the earlier mark of a finished week still takes the nut back
undo(S, weekly, '2026-10-12');
assert.strictEqual(S.nuts, 0);

assert.deepStrictEqual(dayProgress(S, '2026-10-15'), { done: 1, fail: 0, slots: 2 });
// a dated habit only asks for slots inside its run; no end means it runs on
const dated = { ...daily, id: 'c', start: '2026-10-10', end: '2026-10-20' };
assert.deepStrictEqual([active(dated, '2026-10-09'), active(dated, '2026-10-10'), active(dated, '2026-10-20'), active(dated, '2026-10-21')], [false, true, true, false]);
assert.strictEqual(active({ ...dated, end: null }, '2030-01-01'), true);
S = fresh(); S.habits = [dated];
assert.strictEqual(dayProgress(S, '2026-10-21').slots, 0);
assert.strictEqual(dayProgress(S, '2026-10-15').slots, 1);
// weekdays: a Mon/Wed/Fri habit is off on Tuesday, and the day off does not break its streak
const mwf = { ...daily, id: 'd', start: '2026-10-12', days: [0, 2, 4] };
assert.deepStrictEqual(['2026-10-12', '2026-10-13', '2026-10-14'].map(d => active(mwf, d)), [true, false, true]);
S = fresh(); S.habits = [mwf];
mark(S, mwf, '2026-10-12', 'done'); mark(S, mwf, '2026-10-14', 'done');
assert.strictEqual(streak(S, mwf, '2026-10-15'), 2); // Thursday is off too
assert.strictEqual(mark(S, mwf, '2026-10-16', 'done').n, 1); // third running day earns the nut
// record: Mon 12, Wed 14, Fri 16 done, Mon 19 failed: 4 slots, best run 3, and the fail ends the current one
mark(S, mwf, '2026-10-19', 'fail');
assert.deepStrictEqual(record(S, mwf, '2026-10-19'), { slots: 4, done: 3, fail: 1, best: 3, now: 0 });
// today's open slot does not end the run
assert.deepStrictEqual(record(S, mwf, '2026-10-16'), { slots: 3, done: 3, fail: 0, best: 3, now: 3 });
console.log('logic ok');
