'use strict';
// Shop: parts for the robot and evolutions for its base.

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
