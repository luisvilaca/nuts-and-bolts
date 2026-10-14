'use strict';
// Diary: the month of pages, the printed page, its toolbar, and the pen canvas.

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

// The printed diary page: dated header, a charge gauge to fill in, ruled lines.
function pageTemplate(date) {
  const d = day(date), doy = Math.round((d - new Date(d.getFullYear(), 0, 1, 12)) / 864e5) + 1;
  const orbits = (cx, cy, rx, ry) => [0, 60, 120].map(a => `<ellipse cx="${cx}" cy="${cy}" rx="${rx}" ry="${ry}" transform="rotate(${a} ${cx} ${cy})"/>`).join('');
  return `<svg viewBox="0 0 ${PAGE_W} ${PAGE_H}" aria-hidden="true">
    <g class="t-art">${orbits(1290, PAGE_H - 330, 250, 88)}<circle cx="1290" cy="${PAGE_H - 330}" r="10"/></g>
    <path class="t-rule" d="${Array.from({ length: 37 }, (_, i) => `M90 ${220 + i * 55}h1420`).join('')}"/>
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

// One line, StarNote style: undo and redo, pen and eraser, inks, paper, bin on the left; the calendar alone on the right. Settings live in popovers.
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
    <div class="tg"><button class="icb" data-act="pundo" aria-label="Undo stroke">${ICON.undo}</button>
      <button class="icb" data-act="predo" aria-label="Redo stroke">${ICON.redo}</button></div>
    <div class="tg">${pop('pen', ICON.diary, 'Pen. Tap again for settings', `<h3>Nib</h3>${seg('size', PEN_SIZES, p.size)}<h3>Stabilizer</h3>${seg('stab', STAB, p.stab)}`, `data-act="pen" aria-pressed="${!ui.erase}"`)}
      ${pop('eraser', ICON.eraser, 'Eraser. Hold for width', `<h3>Eraser width</h3>${seg('eraser', ERASER_SIZES, p.eraser)}`, `data-act="erase" data-hold="eraser" aria-pressed="${ui.erase}"`)}</div>
    <div class="swatches" role="radiogroup" aria-label="Ink colour">${PEN_COLORS.map(([n, c]) => sw('color', c, !ui.erase && c === p.color, c, n)).join('')}</div>
    <div class="tg">${pop('paper', ICON.paper, 'Paper colours', `<h3>Paper colours</h3>${S.papers.map((c, i) => `<label class="hexrow"><span class="sw sq" data-slot="${i}" style="--c:${c}"></span>
      <input data-hex="${i}" value="${c}" maxlength="7" spellcheck="false" autocomplete="off" autocapitalize="characters" aria-label="Paper ${i + 1}, hex colour"></label>`).join('')}
      <button class="btn" data-act="paperReset">Restore the originals</button>`)}
      <div class="swatches" role="radiogroup" aria-label="Paper">${S.papers.map((c, i) => sw('paper', i, same(c, ui.paper), c, 'Paper ' + (i + 1), `data-slot="${i}"`)).join('')}</div></div>
    <button class="icb" data-act="pclear" aria-label="Bin this page">${ICON.trash}</button>
    <span class="grow"></span>
    <button class="icb" data-act="back" aria-label="Back to calendar">${ICON.calendar}</button>`);
  Object.assign(ink.tool, { color: p.color, size: p.size, stab: p.stab, eraser: p.eraser, erase: ui.erase });
}

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
