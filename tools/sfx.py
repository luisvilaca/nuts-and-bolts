# Synthesizes the app's sound effects as 16-bit mono WAVs: a robot's workshop powered by a small reactor, so servos,
# ratchets, relays, metal, Geiger ticks and reactor hum. Deterministic (fixed seed), no dependencies.
# python tools/sfx.py app/src/workshop/res/raw
import math, random, struct, sys, wave
SR = 44100
random.seed(7)
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

# a sweep's phase: frequency moving linearly from f0 to f1 over dur
def sweep(t, f0, f1, dur):
    u = min(t, dur)
    return TAU * (f0 * u + (f1 - f0) * u * u / (2 * dur)) + (TAU * f1 * (t - dur) if t > dur else 0)

def square(ph, soft=6):  # a band-limited-ish square: the robot's beeper
    return sum(math.sin(ph * k) / k for k in range(1, soft * 2, 2))

# Geiger counter: tiny clicks at random moments, denser where rate(t) is higher
def geiger(times):
    return lambda t: sum(click(t, a, 4200, 0.0007) for a in times if 0 <= t - a < 0.006)
def ticks(start, end, n):
    return sorted(random.uniform(start, end) for _ in range(n))

# the reactor's hum: a low tone with its harmonics and a slow beat
def hum(t, f=60):
    return sum(g * math.sin(TAU * f * k * t) for k, g in ((1, 1), (2, .6), (3, .35), (5, .15))) * (0.85 + 0.15 * math.sin(TAU * 5 * t))

def servo(t, f0, f1, dur):  # a servo motor: a buzzing sweep with gear grit
    if t > dur: return 0
    return (square(sweep(t, f0, f1, dur), 3) * 0.6 + nz(t, 0.5) * 0.15) * math.sin(math.pi * t / dur)

# Done: a short servo turn drives the bolt, three ratchet clicks, then it seats with a metal clunk
write("sfx_done", 0.5, lambda t: servo(t, 180, 420, 0.12) * 0.35
      + sum(click(t, a, 3000 + 400 * k) * 0.5 for k, a in enumerate((.1, .15, .2)))
      + (0 if t < .24 else bell(t - .24, 330, .09) * .8 + math.sin(TAU * 110 * (t - .24)) * math.exp(-(t - .24) / .06)))

# All in: the frame goes critical. The reactor hum swells and climbs, Geiger ticks thicken, and a Cherenkov shimmer
# (a cluster of glassy bells with a tremolo) blooms on top
crit = geiger(ticks(0.05, 0.55, 26))
write("sfx_critical", 1.3, lambda t: hum(t, 55 + 30 * min(t / .6, 1)) * min(t / .5, 1) * math.exp(-max(0, t - .6) / .3) * .5
      + crit(t) * .7
      + sum(bell(t - .45 - i * .05, f, .5) * (0.6 + 0.4 * math.sin(TAU * 7 * t)) for i, f in enumerate((1318.5, 1567.98, 1975.5, 2637))) * .35, gain=.55)

# Skip: a relay clicks over and back
write("sfx_skip", 0.12, lambda t: click(t, 0, 1800, .004) + click(t, .055, 1500, .004) * .7, gain=.35)

# Fail: the bolt shears (a crack of metal), the robot's motor sags, and the counter crackles
leak = geiger(ticks(0.12, 0.5, 9))
write("sfx_fail", 0.6, lambda t: nz(t, 3) * math.exp(-t / .03) * .7 + bell(t, 1700, .04, ((1, 1), (1.53, .7), (2.3, .5))) * .35
      + square(sweep(t, 520, 110, .45), 4) * env(t, .01, .18) * .35 + leak(t) * .5, gain=.5)

# Level up: the robot's beeper climbs an arpeggio over a swell of reactor hum
notes = (523.25, 659.25, 783.99, 1046.5)
write("sfx_level", 1.2, lambda t: sum(square(TAU * f * (t - i * .09), 4) * env(t - i * .09, .004, .12 if i < 3 else .4) * .3 for i, f in enumerate(notes) if t >= i * .09)
      + sum(bell(t - i * .09, f * 2, .3) * .25 for i, f in enumerate(notes) if t >= i * .09)
      + hum(t, 80) * math.sin(math.pi * min(t / 1.2, 1)) * .25)

# Level down: power failing, a falling whine whose tremolo slows as it dies
write("sfx_down", 0.8, lambda t: math.sin(sweep(t, 420, 60, .7)) * (0.6 + 0.4 * math.sin(TAU * (14 - 12 * t) * t)) * env(t, .01, .3)
      + nz(t, 0.25) * .05 * math.exp(-t / .2), gain=.5)

# Nut: two bright clinks of brass
write("sfx_nut", 0.45, lambda t: bell(t, 2300, .06, ((1, 1), (2.4, .6), (3.9, .4))) + bell(t - .08, 2900, .08, ((1, 1), (2.4, .6), (3.9, .4))), gain=.4)

# Buy: the parts tube sighs open, a crate lands, two clinks
write("sfx_buy", 0.7, lambda t: nz(t) * (0.5 if t < .18 else 0) * math.sin(math.pi * min(t / .18, 1)) * .4
      + (0 if t < .18 else math.sin(TAU * 90 * (t - .18)) * math.exp(-(t - .18) / .05) + bell(t - .18, 260, .05) * .5)
      + bell(t - .32, 1900, .07, ((1, 1), (2.4, .6), (3.9, .4))) * .6 + bell(t - .41, 2400, .07, ((1, 1), (2.4, .6), (3.9, .4))) * .6, gain=.5)

# Equip: a servo swings the part in and a latch snaps
write("sfx_equip", 0.4, lambda t: servo(t, 250, 600, .22) * .5 + click(t, .24, 2600, .006) + bell(t - .24, 520, .05) * .4, gain=.45)

# Undo: the ratchet run backwards, a short falling blip
write("sfx_undo", 0.16, lambda t: math.sin(TAU * (900 - 2500 * t) * t) * env(t, .004, .04) + click(t, .02, 2200, .003) * .4, gain=.35)

# New habit: a frame is bolted to the bench, three quick taps rising, and the robot chirps
write("sfx_add", 0.55, lambda t: sum(click(t, a, 2400 + 500 * k, .005) * .6 + bell(t - a, 400 + 120 * k, .04) * .4 for k, a in enumerate((0, .07, .14)))
      + (0 if t < .26 else math.sin(TAU * (1200 + 600 * math.sin(TAU * 9 * (t - .26))) * (t - .26)) * env(t - .26, .01, .07) * .5), gain=.5)

# Delete: unbolted, the frame lifts off and clanks into the bin
write("sfx_delete", 0.6, lambda t: sum(click(t, a, 2600 - 400 * k, .004) * .5 for k, a in enumerate((0, .05, .1)))
      + (0 if t < .2 else bell(t - .2, 196, .12, ((1, 1), (2.1, .6), (3.4, .3))) * .8 + nz(t, 2) * math.exp(-(t - .2) / .02) * .4), gain=.5)

# Chirp: the robot answers a tap, bee-boop
write("sfx_chirp", 0.36, lambda t: math.sin(TAU * (1400 + 120 * math.sin(TAU * 30 * t)) * t) * env(t, .006, .05) * (t < .14)
      + (0 if t < .15 else math.sin(TAU * (900 + 90 * math.sin(TAU * 30 * t)) * (t - .15)) * env(t - .15, .006, .07)), gain=.45)

# Tab: the faintest relay tick
write("sfx_tab", 0.04, lambda t: click(t, 0, 2200, .0025), gain=.18)

# Bin a page: decontamination, a hiss that swells and drains away
write("sfx_bin", 0.7, lambda t: (nz(t) * 0.7 + nz(t, 0.5) * 0.3) * math.sin(math.pi * min(t / .7, 1)) ** 2, gain=.3)

# Reminder notification: the robot calls three notes, the counter ticks under it
call = geiger(ticks(0.0, 0.7, 7))
write("sfx_notify", 0.9, lambda t: sum(square(TAU * f * (t - a), 3) * env(t - a, .005, .09) * .4 for a, f in ((0, 880), (.14, 1318.5), (.28, 1046.5)) if t >= a)
      + sum(bell(t - a, f, .25) * .3 for a, f in ((0, 880), (.14, 1318.5), (.28, 1046.5))) + call(t) * .3, gain=.6)
print("ok")
