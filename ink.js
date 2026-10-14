'use strict';
// Pen canvas for the diary. Strokes are kept as vectors in page units so a page redraws sharp at any size.

// The owner's StarNote palette. Matte Black is also the first paper, so it is the ink for light paper.
const PEN_COLORS = [['Ice Blue', '#E0F7FA'], ['Matrix Green', '#00FF88'], ['Electric Cyan', '#00E5FF'], ['Neon Fuchsia', '#FF007F'], ['Matte Black', '#393939']];
// Three paper slots; the owner can replace any of them with a hex value.
const PAPERS = ['#393939', '#F4ECD8', '#0B1020'];
const PEN_SIZES = [['Fine', 2.4], ['Medium', 4], ['Bold', 6.5]];
const ERASER_SIZES = [['Small', 12], ['Medium', 24], ['Large', 48]]; // page units across
// Fountain pen: the nib opens with pressure and closes with speed, easing between the two so the line swells and tapers.
// shortcut: tuned by eye in a desktop browser, retune min/max/speed on the tablet with the real pen
const NIB = { min: 0.2, max: 1.7, speed: 0.45, ease: 0.35 };
const STAB = [['Off', 1], ['Low', 0.5], ['High', 0.22]]; // share of the gap to the pen closed per sample
const PAGE_W = 1600, PAGE_H = 2260; // A4 proportions: the page fills the width and scrolls

function Ink(canvas, scroller, onChange) {
  // No `desynchronized` hint: on Android it can composite the canvas opaque, hiding the paper underneath.
  const ctx = canvas.getContext('2d');
  // pen: a pen has been used on this device, so fingers scroll the page instead of drawing
  const ink = { strokes: [], pen: false, tool: { color: PEN_COLORS[0][1], size: 1, erase: false, eraser: 1, stab: 1 } };
  let redo = [], cur = null, pid = null, sm = null, last = null, lastT = 0, nib = 0, pan = null;

  // v is the nib opening, 0 to 1, stored as each point's third value. An eraser stroke stores a fifth of its width in w.
  const width = (s, v) => s.e ? s.w * 5 : s.w * (NIB.min + (NIB.max - NIB.min) * v);
  function style(s) {
    ctx.globalCompositeOperation = s.e ? 'destination-out' : 'source-over';
    ctx.strokeStyle = ctx.fillStyle = s.c;
    ctx.lineCap = ctx.lineJoin = 'round';
  }
  // Draws the piece of s ending at point i (points are flat x, y, pressure triples), curving through midpoints.
  function seg(s, i) {
    const p = s.p, a = (i - 1) * 3, b = i * 3;
    style(s);
    ctx.beginPath();
    if (i === 0) { ctx.arc(p[0], p[1], width(s, p[2]) / 2, 0, 7); ctx.fill(); return; }
    const mx = (p[a] + p[b]) / 2, my = (p[a + 1] + p[b + 1]) / 2;
    if (i === 1) ctx.moveTo(p[0], p[1]), ctx.lineTo(mx, my);
    else ctx.moveTo((p[a - 3] + p[a]) / 2, (p[a - 2] + p[a + 1]) / 2), ctx.quadraticCurveTo(p[a], p[a + 1], mx, my);
    ctx.lineWidth = width(s, (p[a + 2] + p[b + 2]) / 2);
    ctx.stroke();
  }
  // seg stops at the last midpoint; this closes the stroke to its final point
  function tail(s) {
    const p = s.p, b = p.length - 3, a = b - 3;
    if (a < 0) return;
    style(s);
    ctx.beginPath();
    ctx.moveTo((p[a] + p[b]) / 2, (p[a + 1] + p[b + 1]) / 2);
    ctx.lineTo(p[b], p[b + 1]);
    ctx.lineWidth = width(s, p[b + 2]);
    ctx.stroke();
  }
  function redraw() {
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const k = canvas.width / PAGE_W;
    ctx.setTransform(k, 0, 0, k, 0, 0);
    for (const s of ink.strokes) { for (let i = 0; i < s.p.length / 3; i++) seg(s, i); tail(s); }
  }

  // the eraser's outline follows the pen, hovering or rubbing, at the size it actually erases
  const ring = canvas.parentNode.appendChild(document.createElement('i'));
  ring.className = 'erase-ring'; ring.hidden = true;
  function showRing(e) {
    ring.hidden = !(ink.tool.erase || e.buttons & 32);
    if (ring.hidden) return;
    const d = ERASER_SIZES[ink.tool.eraser][1] * canvas.clientWidth / PAGE_W;
    ring.style.cssText = `width:${d}px;height:${d}px;left:${e.offsetX}px;top:${e.offsetY}px`;
  }
  canvas.addEventListener('pointerleave', () => { ring.hidden = true; });

  const pt = e => {
    const r = canvas.getBoundingClientRect();
    return [(e.clientX - r.left) * PAGE_W / r.width, (e.clientY - r.top) * PAGE_H / r.height, e.pointerType === 'pen' ? e.pressure || 0.1 : 0.5];
  };
  function put(q) {
    const p = cur.p, n = p.length;
    if (n && Math.hypot(q[0] - p[n - 3], q[1] - p[n - 2]) < 0.5) return;
    p.push(Math.round(q[0] * 10) / 10, Math.round(q[1] * 10) / 10, Math.round(q[2] * 100) / 100);
    seg(cur, p.length / 3 - 1);
  }

  canvas.addEventListener('pointerdown', e => {
    if (e.pointerType === 'pen') ink.pen = true;
    // palm rejection: once a pen has been used, a finger drags the page instead of drawing
    else if (e.pointerType === 'touch' && ink.pen) { pan = { id: e.pointerId, y: e.clientY }; return; }
    if (cur) return;
    e.preventDefault();
    showRing(e);
    canvas.setPointerCapture(pid = e.pointerId);
    const erase = ink.tool.erase || e.buttons & 32; // buttons bit 32 is the pen's eraser end
    cur = { c: ink.tool.color, w: erase ? ERASER_SIZES[ink.tool.eraser][1] / 5 : PEN_SIZES[ink.tool.size][1], e: erase ? 1 : 0, p: [] };
    last = pt(e); lastT = e.timeStamp; sm = [...last];
    nib = last[2] * 0.3; // touch down closed, so the stroke tapers in
    put([sm[0], sm[1], nib]);
  });
  canvas.addEventListener('pointermove', e => {
    if (pan && e.pointerId === pan.id) { scroller.scrollTop -= e.clientY - pan.y; pan.y = e.clientY; return; }
    showRing(e);
    if (!cur || e.pointerId !== pid) return;
    const evs = e.getCoalescedEvents?.() || [], k = STAB[ink.tool.stab][1];
    for (const ev of evs.length ? evs : [e]) {
      const raw = pt(ev), speed = Math.hypot(raw[0] - last[0], raw[1] - last[1]) / Math.max(1, ev.timeStamp - lastT); // page units per ms
      nib += (Math.min(1, raw[2] * Math.max(0.45, 1.15 - NIB.speed * speed)) - nib) * NIB.ease;
      last = raw; lastT = ev.timeStamp;
      // stabilizer: the drawn point trails the pen, which irons out jitter
      sm[0] += (raw[0] - sm[0]) * k; sm[1] += (raw[1] - sm[1]) * k;
      put([sm[0], sm[1], nib]);
    }
  });
  const end = e => {
    if (pan && e.pointerId === pan.id) { pan = null; return; }
    if (!cur || e.pointerId !== pid) return;
    if (cur.p.length === 3) { cur.p[2] = last[2]; seg(cur, 0); } // a tap is a full dot, not a closed nib
    else if (e.type === 'pointerup') { const q = pt(e); put([q[0], q[1], nib * 0.35]); } // catch up to where the pen lifted, tapering out
    tail(cur);
    ink.strokes.push(cur);
    redo = []; cur = null;
    onChange();
  };
  canvas.addEventListener('pointerup', end);
  canvas.addEventListener('pointercancel', end);
  canvas.addEventListener('contextmenu', e => e.preventDefault());

  ink.resize = (w, h) => {
    // stay inside what mobile GPUs take as one texture: 4096px tall, about 12 megapixels
    const d = Math.min(window.devicePixelRatio || 1, 4096 / h, Math.sqrt(12e6 / (w * h)));
    canvas.width = Math.round(w * d); canvas.height = Math.round(h * d);
    redraw();
  };
  ink.load = strokes => { ink.strokes = strokes; redo = []; redraw(); };
  ink.undo = () => { if (ink.strokes.length) { redo.push(ink.strokes.pop()); redraw(); onChange(); } };
  ink.redo = () => { if (redo.length) { ink.strokes.push(redo.pop()); redraw(); onChange(); } };
  ink.clear = () => { ink.strokes = []; redo = []; redraw(); onChange(); };
  return ink;
}
