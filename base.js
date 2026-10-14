'use strict';
// Base: the robot at home, today's habits, attributes and the week.

function renderBase() {
  setHTML($('#device'), deviceHTML());
  const t = today(), n = $('#botName');
  setHTML($('#baseScene'), scene());
  if (document.activeElement !== n) n.value = S.name;
  $('#mood').textContent = mood()[0];
  $('#baseTier').textContent = PART[S.equip.base].no + ' · ' + PART[S.equip.base].name;
  const run = S.habits.filter(h => active(h, t));
  setHTML($('#baseToday'), run.length
    ? `<ul class="mini">${run.map(h => `<li><span>${esc(h.name)}</span><span class="h-slots">${slots(h, t, false)}</span></li>`).join('')}</ul>`
    : `<p class="note">${S.habits.length ? 'No habits run today.' : `No habits yet. Add one and ${esc(S.name)} starts earning bolts.`}</p>`);
  setHTML($('#attrs'), Object.entries(STATS).map(([k, d]) => `<li>
    <span class="a-ico">${ICON[k]}</span><span class="a-name">${d.name}<small>${d.gloss}</small></span>
    <span class="meter" style="--v:${S.stats[k]}%" role="meter" aria-label="${d.name}" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${S.stats[k]}"></span>
    <b>${S.stats[k]}</b></li>`).join(''));
  setHTML($('#week'), periodDates({ per: 'week' }, t).map(s => `<li${s === t ? ' class="is-today"' : ''} aria-label="${fmt(s, { weekday: 'long' })}${dot(s)[1]}">
    <span class="cell">${dot(s)[0]}</span>${fmt(s, { weekday: 'short' })}</li>`).join(''));
}
