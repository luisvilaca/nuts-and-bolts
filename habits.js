'use strict';
// Habits: the day list, the calendar, each habit's history, and the habit form.

function habitRow(h, d) {
  const ms = periodMarks(S, h, d), off = ms.length >= h.target || d > today(), st = streak(S, h, d);
  return `<li class="habit">
    <button class="h-main" data-act="edit" data-id="${h.id}" aria-label="Edit ${esc(h.name)}">
      <span class="h-ico">${ICON[h.stat]}</span>
      <span class="h-name">${esc(h.name)}${h.kind === 'bad' ? '<i class="tag">break</i>' : ''}</span>
      <span class="h-meta">${h.target} per ${h.per} · ${STATS[h.stat].name} · ${plural(WEIGHTS[h.weight].bolts, 'bolt')}${h.days ? ' · ' + h.days.map(i => DOW[i]).join(' ') : ''}${h.end ? ` · until ${fmt(h.end, { day: 'numeric', month: 'short' })}` : ''}${st > 1 ? ` · ${st} ${h.per}s running` : ''}</span>
    </button>
    <div class="h-slots">${slots(h, d, true)}</div>
    <div class="h-acts">${['done', 'skip', 'fail'].map(s =>
      `<button class="btn ${s}" data-act="mark" data-id="${h.id}" data-s="${s}"${off ? ' disabled' : ''}>${BTN[h.kind][s]}</button>`).join('')}</div>
  </li>`;
}

function formHTML(h) {
  const v = h || { name: '', kind: 'good', per: 'day', target: 1, stat: 'battery', weight: 'medium', start: ui.date };
  // an undated habit has run since its first mark
  const start = v.start || Object.keys(S.marks).sort().find(d => S.marks[d][h.id]) || today();
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
    <fieldset class="field"><legend>Weight</legend>
      <div class="seg wide">${Object.entries(WEIGHTS).map(([k, w]) => radio('weight', k, v.weight, `<span>${w.name}<small>${plural(w.bolts, 'bolt')}, ${plural(w.pts, 'point')}</small></span>`)).join('')}</div>
      <p class="hint">Done pays this much. Failed takes the same amount back.</p></fieldset>
    <fieldset class="field full"><legend>Attribute it trains</legend>
      <div class="seg wide">${Object.entries(STATS).map(([k, d]) => radio('stat', k, v.stat, `<span>${ICON[k]}${d.name}<small>${d.gloss}</small></span>`)).join('')}</div></fieldset>
    <fieldset class="field full"><legend>Runs</legend>
      <div class="row"><span>From</span><input name="start" type="date" required value="${start}" aria-label="Start date">
        <span>until</span><div class="seg">${radio('ends', 'never', v.end ? 'on' : 'never', '<span>Indefinitely</span>')}${radio('ends', 'on', v.end ? 'on' : 'never', '<span>A set day</span>')}</div>
        <input name="end" type="date" value="${v.end || ''}" min="${start}" aria-label="End date"></div>
      <div class="row days"><span>On</span><div class="seg">${DOW.map((n, i) => `<label><input type="checkbox" name="days" value="${i}"${!v.days || v.days.includes(i) ? ' checked' : ''}><span>${n}</span></label>`).join('')}</div></div></fieldset>
    <div class="row">${h ? '<button type="button" class="btn danger" data-act="del">Delete habit</button>' : ''}<span class="grow"></span>
      <button type="button" class="btn" data-act="cancel">Cancel</button><button class="btn primary">${h ? 'Save changes' : 'Add habit'}</button></div>
  </form>`;
}

// Every habit's lifetime: share of slots done, fails, best and current run.
function historyHTML() {
  const t = today();
  return `<ul class="list">${S.habits.map(h => {
    const r = record(S, h, t), pct = r.slots ? Math.round(r.done / r.slots * 100) : 0, unit = n => plural(n, h.per);
    return `<li class="hist"><span class="h-ico">${ICON[h.stat]}</span>
      <span><span class="h-name">${esc(h.name)}</span><span class="h-meta">${r.done} of ${plural(r.slots, 'slot')} done${r.fail ? ` · ${r.fail} failed` : ''} · best run ${unit(r.best)}${r.now ? ` · ${unit(r.now)} running now` : ''}</span></span>
      <span class="meter" style="--v:${pct}%" role="meter" aria-label="${esc(h.name)}, done" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${pct}"></span><b>${pct}%</b></li>`;
  }).join('')}</ul>`;
}

function renderHabits() {
  const t = today(), d = ui.date, p = dayProgress(S, d);
  setHTML($('#habScene'), scene());
  $('#habDay').textContent = d === t ? 'Today' : fmt(d, { weekday: 'long' });
  $('#habDate').textContent = fmt(d, { day: 'numeric', month: 'long' });
  $('#habSum').textContent = !S.habits.length ? 'No habits yet' : d > t ? `${plural(p.slots, 'slot')} planned` : `${p.done} of ${p.slots} fastened` + (p.fail ? `, ${p.fail} failed` : '');
  if (ui.form) { setHTML($('#habBar'), ''); setHTML($('#habBody'), formHTML(habit(ui.form.id))); return; }
  const v = ui.view, run = S.habits.filter(h => active(h, d));
  // every day is open, past and future, to see which habits run on it
  setHTML($('#habBar'), `${v === 'cal' ? navHTML(fmt(ui.month + '-01', { month: 'long', year: 'numeric' }))
      : v === 'day' ? navHTML(fmt(d, { weekday: 'short', day: 'numeric', month: 'short' })) : ''}
    ${v === 'day' && d !== t ? '<button class="btn" data-act="today">Back to today</button>' : ''}<span class="grow"></span>
    <div class="seg">${[['day', 'Day'], ['cal', 'Calendar'], ['hist', 'History']].map(([k, n]) => `<button data-act="view" data-v="${k}" aria-pressed="${v === k}">${n}</button>`).join('')}</div>
    <button class="btn primary" data-act="new">${ICON.plus}Add habit</button>`);
  setHTML($('#habBody'), v === 'cal' ? monthHTML(dot, true)
    : v === 'hist' && S.habits.length ? historyHTML()
    : run.length ? `<ul class="list">${run.map(h => habitRow(h, d)).join('')}</ul>`
    : S.habits.length ? `<p class="note">No habits run on this day.</p>`
    : `<div class="empty"><h2>Nothing to fasten yet</h2>
        <p>A habit is something you want to do, or stop doing, a set number of times per day or week. Each one feeds ${esc(S.name)}.</p>
        <button class="btn primary" data-act="new">${ICON.plus}Add your first habit</button>
        <p>Or start from one of these:</p>
        <div class="chips">${SUGGEST.map((s, i) => `<button class="btn" data-act="suggest" data-i="${i}">${s.name} · ${s.target} per ${s.per}</button>`).join('')}</div></div>`);
}
