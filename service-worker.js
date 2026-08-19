const CACHE_NAME = "ncm-permits-cache-v28";
const CORE_ASSETS = [
  "./index.html",
  "./manifest.json",
  "./papaparse.min.js",
  "./icon-192.png",
  "./icon-512.png",
  "./icon-maskable-512.png"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(CORE_ASSETS)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  const url = event.request.url;

  // Network-first everywhere: always get the latest page/data when online,
  // and only fall back to the cached copy if there's no connection at all.
  event.respondWith(
    fetch(event.request)
      .then((response) => {
        // Only cache same-origin, successful responses. Opaque cross-origin
        // responses (fonts, sheet data) are left alone.
        if (response && response.ok && response.type === "basic" && !url.includes("docs.google.com")) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
        }
        return response;
      })
      .catch(() => caches.match(event.request))
  );
});
