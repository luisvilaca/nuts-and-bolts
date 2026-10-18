# Synthesizes the app's sound effects as 16-bit mono WAVs. Deterministic (fixed seed), no dependencies.
import math, random, struct, sys, wave
SR = 44100
random.seed(7)
out = sys.argv[1]

def env(t, a, d):  # attack, exponential decay
    return (t / a if t < a else 1.0) * math.exp(-(max(0, t - a)) / d)

def bell(t, f, d, parts=((1, 1), (2.76, .5), (5.4, .25), (8.93, .12))):
    return sum(g * math.sin(2 * math.pi * f * r * t) * math.exp(-t * r / d) for r, g in parts)

def write(name, dur, fn, gain=0.6):
    n = int(SR * dur); xs = [fn(i / SR) for i in range(n)]
    peak = max(1e-9, max(abs(x) for x in xs))
    fade = int(SR * 0.006)
    with wave.open(f"{out}/{name}.wav", "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(32767 * gain * x / peak * min(1, (n - i) / fade))) for i, x in enumerate(xs)))

noise = [random.uniform(-1, 1) for _ in range(SR)]
def click(t, at, f=3200, d=0.004):
    u = t - at
    return 0 if u < 0 else (noise[int(u * SR) % SR] * 0.6 + math.sin(2 * math.pi * f * u)) * math.exp(-u / d)

# Done: three ratchet clicks as the bolt turns, then it seats with a low metal clunk
write("sfx_done", 0.45, lambda t: sum(click(t, a, 3000 + 400 * k) * 0.5 for k, a in enumerate((0, .05, .1)))
      + (0 if t < .15 else bell(t - .15, 330, .09) * .8 + math.sin(2 * math.pi * 110 * (t - .15)) * math.exp(-(t - .15) / .06)))
# Skip: one soft tick
write("sfx_skip", 0.1, lambda t: click(t, 0, 1800, .006), gain=.35)
# Fail: a short shear of noise and a falling, dull ping
write("sfx_fail", 0.5, lambda t: noise[int(t * SR * 3) % SR] * math.exp(-t / .05) * .6
      + math.sin(2 * math.pi * (420 - 260 * min(t / .4, 1)) * t) * env(t, .005, .14), gain=.5)
# Level up: a rising arpeggio of bells with a shimmer on top
notes = (523.25, 659.25, 783.99, 1046.5)
write("sfx_level", 1.2, lambda t: sum(bell(t - i * .09, f, .35) * (.7 if i < 3 else 1) for i, f in enumerate(notes) if t >= i * .09)
      + (0 if t < .27 else math.sin(2 * math.pi * 2093 * t) * math.sin(2 * math.pi * 6 * t) * .15 * math.exp(-(t - .27) / .3)))
# Buy: two coin clinks
write("sfx_buy", 0.5, lambda t: sum(bell(t - a, f, .07, ((1, 1), (2.4, .6), (3.9, .4), (5.6, .2))) for a, f in ((0, 1900), (.09, 2400)) if t >= a), gain=.45)
# Undo: a short falling blip
write("sfx_undo", 0.14, lambda t: math.sin(2 * math.pi * (900 - 2500 * t) * t) * env(t, .004, .04), gain=.35)
print("ok")
