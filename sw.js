// Network first, so an update lands the next time the tablet is online; the cache is the offline copy.
const CACHE = 'nb-v3';
const FILES = ['./', 'style.css', 'logic.js', 'robot.js', 'ink.js', 'app.js', 'fonts/oxanium.woff2', 'fonts/exo2.woff2', 'icon.svg', 'bg.svg', 'manifest.webmanifest'];

self.addEventListener('install', e => e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting())));
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  e.respondWith(
    fetch(e.request)
      .then(r => { const copy = r.clone(); caches.open(CACHE).then(c => c.put(e.request, copy)); return r; })
      .catch(() => caches.match(e.request, { ignoreSearch: true }))
  );
});
