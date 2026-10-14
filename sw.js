// Network first, so an update lands the next time the tablet is online; the cache is the offline copy.
const CACHE = 'nb-v6';
const FILES = ['./', 'style.css', 'logic.js', 'robot.js', 'ink.js', 'core.js', 'base.js', 'shop.js', 'habits.js', 'diary.js', 'device.js', 'app.js', 'fonts/oxanium.woff2', 'fonts/exo2.woff2', 'icon.svg', 'bg.svg', 'manifest.webmanifest'];

self.addEventListener('install', e => e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting())));
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));
// Daily reminder. Android wakes this when it chooses, for an installed app it trusts; it nudges at most once a day,
// after the set time, when nothing has been logged. The page keeps the settings in the nb-meta cache (see device.js).
self.addEventListener('periodicsync', e => {
  if (e.tag !== 'remind') return;
  e.waitUntil((async () => {
    const c = await caches.open('nb-meta'), r = await c.match('reminder'), last = await c.match('nudged');
    if (!r) return;
    const m = await r.json(), d = new Date(), p = n => String(n).padStart(2, '0');
    const t = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`, hm = `${p(d.getHours())}:${p(d.getMinutes())}`;
    if (!m.on || m.logged === t || hm < m.time || (last && await last.text()) === t) return;
    await self.registration.showNotification('Nuts & Bolts', { body: 'Nothing logged today yet. Your robot is waiting.', icon: 'icon-192.png', tag: 'remind' });
    await c.put('nudged', new Response(t));
  })());
});
self.addEventListener('notificationclick', e => {
  e.notification.close();
  e.waitUntil(clients.matchAll({ type: 'window' }).then(ws => ws[0] ? ws[0].focus() : clients.openWindow('./')));
});
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  e.respondWith(
    fetch(e.request)
      .then(r => { const copy = r.clone(); caches.open(CACHE).then(c => c.put(e.request, copy)); return r; })
      .catch(() => caches.match(e.request, { ignoreSearch: true }))
  );
});
