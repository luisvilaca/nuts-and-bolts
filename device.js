'use strict';
// This tablet: a backup file out and back in, and the daily reminder.

const BACKUP = 'nuts-and-bolts';

function deviceHTML() {
  const r = S.remind, on = r.on ? 'on' : 'off';
  return `<div class="row"><button class="btn" data-act="backup">Save a backup</button>
      <label class="btn">Restore a backup<input type="file" accept=".json,application/json" data-restore hidden></label>
      <span class="note">${S.backedUp ? 'Last backup ' + fmt(S.backedUp, { day: 'numeric', month: 'long' }) : 'Never backed up'}</span></div>
    <p class="hint">A backup is one file with every habit, mark, part and diary page. Keep a copy off the tablet, in Drive for example.</p>
    <div class="row"><span>Daily reminder</span>
      <div class="seg">${radio('remind', 'off', on, '<span>Off</span>')}${radio('remind', 'on', on, '<span>On</span>')}</div>
      <input type="time" name="remindAt" value="${r.time}" aria-label="Reminder time"${r.on ? '' : ' disabled'}></div>
    <p class="hint">Nudges once a day after this time if nothing is logged yet. Android decides when the app may check, so it can arrive late. Needs the app on the home screen.</p>`;
}

// Everything in one file: the saved state and every diary page.
async function backup() {
  try {
    const [keys, vals] = await Promise.all([idb('readonly', s => s.getAllKeys()), idb('readonly', s => s.getAll())]);
    const pages = Object.fromEntries(keys.map((k, i) => [k, vals[i]]));
    const blob = new Blob([JSON.stringify({ app: BACKUP, v: 1, saved: today(), state: S, pages })], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob); a.download = `nuts-and-bolts-${today()}.json`; a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 5000);
    S.backedUp = today(); save(); render();
    toast('Backup saved to Downloads');
  } catch { toast('The backup could not be made.'); }
}

// A backup file is untrusted until it checks out; nothing on the tablet changes before then.
async function restore(file) {
  let b = null;
  try { b = JSON.parse(await file.text()); } catch {}
  const isDate = k => /^\d{4}-\d\d-\d\d$/.test(k);
  const ok = b?.app === BACKUP && b.state && typeof b.state === 'object' && Array.isArray(b.state.habits) && b.pages && typeof b.pages === 'object'
    && Object.entries(b.pages).every(([k, v]) => isDate(k) && v && (Array.isArray(v) || Array.isArray(v.strokes)));
  if (!ok) return toast('That file is not a Nuts & Bolts backup.');
  const when = isDate(b.saved || '') ? ` from ${fmt(b.saved, { day: 'numeric', month: 'long', year: 'numeric' })}` : '';
  if (!confirm(`Replace everything on this tablet with the backup${when}? Habits, robot and diary pages written since then are lost.`)) return;
  try {
    // one transaction: the old pages are cleared only if every page from the file is written
    await db.then(d => new Promise((res, rej) => {
      const tx = d.transaction('pages', 'readwrite'), st = tx.objectStore('pages');
      st.clear();
      for (const [k, v] of Object.entries(b.pages)) st.put(v, k);
      tx.oncomplete = res; tx.onerror = () => rej(tx.error);
    }));
  } catch { return toast('The backup could not be restored. Nothing was changed.'); }
  S = upgrade(b.state);
  Object.assign(ui, { page: null, form: null, pop: null });
  save(); render();
  toast('Backup restored');
}

// The service worker cannot read localStorage, so the reminder's settings and today's status go to a cache it can read.
let remindSent = '';
function syncReminder() {
  const t = today(), m = JSON.stringify({ ...S.remind, logged: S.marks[t] || S.diary[t] ? t : '' });
  if (m === remindSent || !('caches' in window)) return;
  remindSent = m;
  caches.open('nb-meta').then(c => c.put('reminder', new Response(m))).catch(() => {});
}

async function setReminder(on) {
  const reg = await navigator.serviceWorker?.ready;
  if (!on) { S.remind.on = false; await reg?.periodicSync?.unregister('remind').catch(() => {}); return; }
  if (!reg?.periodicSync || typeof Notification === 'undefined') return toast('This browser cannot remind in the background.');
  if (await Notification.requestPermission() !== 'granted') return toast('Allow notifications for the app to get the reminder.');
  try { await reg.periodicSync.register('remind', { minInterval: 60 * 60 * 1000 }); }
  catch { return toast('Add the app to the home screen first, then turn the reminder on.'); }
  S.remind.on = true;
}

document.addEventListener('change', async e => {
  const t = e.target;
  if (t.matches('[data-restore]')) { if (t.files[0]) await restore(t.files[0]); t.value = ''; return; }
  if (t.name === 'remind') await setReminder(t.value === 'on');
  else if (t.name === 'remindAt' && t.value) S.remind.time = t.value;
  else return;
  $('#device')._h = null; // redraw even if nothing changed, so a refused switch flips back to Off
  save(); render();
});
