# Synthesizes the circus flavour's sound effects as 16-bit mono WAVs: a big top after dark, so marquee bulbs buzzing
# on, a wheezing calliope, a music box, glass, a clown horn, camera static and animatronic servos. Original sounds,
# made here; nothing is taken from any game. Deterministic (fixed seed), no dependencies.
# python tools/sfx_circus.py app/src/circus/res/raw
import math, random, struct, sys, wave
SR = 44100
random.seed(13)
out = sys.argv[1]
TAU = 2 * math.pi

def env(t, a, d):  # attack, exponential decay
    return (t / a if t < a else 1.0) * math.exp(-(max(0, t - a)) / d)

def bell(t, f, d, parts=((1, 1), (2.76, .5), (5.4, .25), (8.93, .12))):
    return 0 if t < 0 else sum(g * math.sin(TAU * f * r * t) * math.exp(-t * r / d) for r, g in parts)

def write(name, dur, fn, gain=0.6):
    n = int(SR * dur); xs = [fn(i / SR) for i in range(n)]
    peak = max(1e-9, max(abs(x) for x in xs))
    fade = int(SR * 0.006)
    with wave.open(f"{out}/{name}.wav", "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(32767 * gain * x / peak * min(1, (n - i) / fade, (i + 1) / 40))) for i, x in enumerate(xs)))

noise = [random.uniform(-1, 1) for _ in range(SR)]
def nz(t, rate=1.0): return noise[int(t * SR * rate) % SR]

def click(t, at, f=3200, d=0.004):
    u = t - at
    return 0 if u < 0 else (nz(u) * 0.6 + math.sin(TAU * f * u)) * math.exp(-u / d)

def sweep(t, f0, f1, dur):
    u = min(t, dur)
    return TAU * (f0 * u + (f1 - f0) * u * u / (2 * dur)) + (TAU * f1 * (t - dur) if t > dur else 0)

def saw(ph, soft=8):
    return sum(math.sin(ph * k) / k for k in range(1, soft + 1))

# a calliope pipe: breathy, a little flat and wobbling, as an old steam organ is
def pipe(t, f, d=0.25, a=0.02):
    if t < 0: return 0
    v = 1 + 0.006 * math.sin(TAU * 5.5 * t)
    tone = sum(g * math.sin(TAU * f * 0.995 * k * v * t) for k, g in ((1, 1), (2, .45), (3, .3), (4, .12)))
    return (tone + nz(t, 1.3) * .12) * env(t, a, d)

# a music box tine: bright, clean, ringing long
def tine(t, f, d=0.6):
    return bell(t, f, d, ((1, 1), (3.0, .18), (5.2, .06)))

# the bulb's hum: mains buzz with its odd harmonics
def buzz(t, f=120):
    return sum(math.sin(TAU * f * k * t) / k for k in (1, 3, 5, 7)) * (0.8 + 0.2 * nz(t, 0.02))

def servo(t, f0, f1, dur):  # an animatronic's servo, whining with gear grit
    if t > dur: return 0
    return (saw(sweep(t, f0, f1, dur), 4) * 0.5 + nz(t, 0.5) * 0.18) * math.sin(math.pi * t / dur)

def glass(t, n=14, seed=3):  # shards: many short high bells at scattered moments
    r = random.Random(seed)
    hits = [(r.uniform(0, .25), r.uniform(2500, 7000)) for _ in range(n)]
    return sum(bell(t - a, f, .03, ((1, 1), (1.7, .6), (2.9, .4))) for a, f in hits if t >= a)

# Done: a marquee bulb buzzes on, flickers once, the filament tinks, and a single calliope note answers
write("sfx_done", 0.6, lambda t: buzz(t) * (0 if .05 < t < .09 else 1) * env(t, .005, .12) * .35
      + bell(t - .1, 3800, .05) * .3 + pipe(t - .12, 523.25, .2) * .55)

# A sign fully lit: a short calliope fanfare, slightly out of tune, and a cymbal of light
fan = ((0, 392.0), (.12, 523.25), (.24, 659.25), (.36, 783.99))
write("sfx_critical", 1.4, lambda t: sum(pipe(t - a, f, .12 if i < 3 else .55) * .5 for i, (a, f) in enumerate(fan))
      + sum(pipe(t - .36, f, .55) * .25 for f in (523.25, 659.25))
      + (0 if t < .36 else nz(t, 1.7) * math.exp(-(t - .36) / .25) * .25), gain=.55)

# Skip: a bulb is unscrewed, glass squeaking in its socket twice
write("sfx_skip", 0.22, lambda t: sum(math.sin(sweep(max(0, t - a), 2600, 3400, .04)) * env(t - a, .004, .02) * (t >= a) for a in (0, .09)) * .5
      + nz(t, 2) * .05, gain=.3)

# Fail: the bulb smashes, then a short animatronic screech falls away into static
write("sfx_fail", 0.75, lambda t: nz(t, 3) * math.exp(-t / .02) * .6 + glass(t) * .35
      + (0 if t < .12 else (saw(sweep(t - .12, 1400, 300, .35), 6) * .45 + nz(t, 1.1) * .35) * env(t - .12, .01, .16))
      + (0 if t < .3 else nz(t, .8) * math.exp(-(t - .3) / .2) * .15), gain=.5)

# Level up: the music box climbs its tune
mb = (783.99, 987.77, 1174.66, 1567.98, 1975.53)
write("sfx_level", 1.5, lambda t: sum(tine(t - i * .11, f) * .4 for i, f in enumerate(mb)), gain=.5)

# Level down: the music box winding down, its last notes slowing and sagging flat
down = ((0, 1174.66), (.18, 987.77), (.42, 830.6), (.75, 698.46))
write("sfx_down", 1.3, lambda t: sum(tine(t - a, f * (1 - .03 * i), .5) * .45 for i, (a, f) in enumerate(down))
      + (0 if t < .9 else math.sin(TAU * 180 * (t - .9)) * env(t - .9, .01, .1) * .2), gain=.5)

# Faz-Token: a coin drops on the counter and the prize bell dings
write("sfx_nut", 0.6, lambda t: sum(bell(t - a, f, .04, ((1, 1), (2.4, .6), (3.9, .4))) * .5 for a, f in ((0, 2600), (.05, 2900), (.09, 2750)))
      + bell(t - .16, 1760, .25, ((1, 1), (2.0, .4), (3.0, .2))) * .7, gain=.45)

# Prize won: the claw drops, the prize thunks into the chute, the bell rings twice
write("sfx_buy", 0.85, lambda t: servo(t, 300, 180, .18) * .4
      + (0 if t < .2 else math.sin(TAU * 85 * (t - .2)) * math.exp(-(t - .2) / .05) + nz(t, 1.5) * math.exp(-(t - .2) / .02) * .3)
      + bell(t - .36, 1760, .2, ((1, 1), (2.0, .4), (3.0, .2))) * .6 + bell(t - .52, 2093, .25, ((1, 1), (2.0, .4), (3.0, .2))) * .6, gain=.5)

# Equip: the costume goes on, a servo turns the head, the jaw clacks shut
write("sfx_equip", 0.45, lambda t: servo(t, 200, 520, .25) * .5 + click(t, .27, 1400, .008) + bell(t - .27, 220, .05) * .5, gain=.45)

# Undo: a servo run backwards
write("sfx_undo", 0.2, lambda t: servo(t, 520, 200, .14) * .6 + click(t, .15, 1800, .003) * .4, gain=.35)

# A sign hung: three hammer taps on the riser and a calliope blip rising
write("sfx_add", 0.6, lambda t: sum(click(t, a, 900 + 200 * k, .01) * .6 for k, a in enumerate((0, .08, .16)))
      + pipe(t - .26, 659.25, .1) * .45 + pipe(t - .34, 880, .14) * .45, gain=.5)

# A sign taken down: it drops, wood clattering, a low thud
write("sfx_delete", 0.6, lambda t: sum(click(t, a, 700 - 100 * k, .012) * .6 for k, a in enumerate((0, .04, .1, .13)))
      + (0 if t < .2 else math.sin(TAU * 70 * (t - .2)) * math.exp(-(t - .2) / .08) + nz(t, .7) * math.exp(-(t - .2) / .03) * .3), gain=.5)

# Chirp: the animatronic answers a tap with a squeeze of its clown horn, honk-honk
def honk(u):
    if u < 0: return 0
    f = 330 + 60 * math.exp(-u / .03)
    return (saw(TAU * f * u, 10) + saw(TAU * f * 1.5 * u, 6) * .4) * env(u, .008, .06) * (u < .12)
write("sfx_chirp", 0.4, lambda t: honk(t) + honk(t - .17) * .9, gain=.4)

# Tab: the security camera switching, a click and a flick of static
write("sfx_tab", 0.06, lambda t: click(t, 0, 1600, .002) * .6 + nz(t, 1.9) * math.exp(-t / .01) * .4, gain=.2)

# Bin a page: a swell of camera static that cuts out
write("sfx_bin", 0.7, lambda t: (nz(t) * .7 + nz(t, .37) * .3) * math.sin(math.pi * min(t / .7, 1)) ** 2 * (1 if (int(t * 40) % 7) else .3), gain=.3)

# Notification: three music box notes of a lullaby, a breath of static under them
write("sfx_notify", 1.2, lambda t: sum(tine(t - a, f, .5) * .45 for a, f in ((0, 1318.51), (.22, 1174.66), (.44, 987.77)))
      + nz(t, .9) * .03 * math.exp(-t / .6), gain=.6)
print("ok")
