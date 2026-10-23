package com.luisvilaca.nutsandbolts

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

// Short effects for the moments that change something, in the skin's own sounds (the workshop's servos, ratchets,
// relays, metal, Geiger ticks and reactor hum; the circus's bulbs, calliope, music box, glass and camera static): a bolt driven home, a frame going critical, a skip, a sheared bolt, a level gained or
// lost, a nut, a purchase, a part equipped, an undo, a habit added or deleted, the robot answering a tap, a tab, a
// page binned. Synthesized by tools/sfx.py and tools/sfx_circus.py into each flavour's res/raw. Silent when the Sounds switch in Settings is off (App keeps on in step
// with it), and before load.
object Sfx {
    private var pool: SoundPool? = null
    var on = true
    private val ids = mutableMapOf<String, Int>()
    private val RAW = mapOf(
        "done" to R.raw.sfx_done, "critical" to R.raw.sfx_critical, "skip" to R.raw.sfx_skip, "fail" to R.raw.sfx_fail,
        "level" to R.raw.sfx_level, "down" to R.raw.sfx_down, "nut" to R.raw.sfx_nut, "buy" to R.raw.sfx_buy,
        "equip" to R.raw.sfx_equip, "undo" to R.raw.sfx_undo, "add" to R.raw.sfx_add, "delete" to R.raw.sfx_delete,
        "chirp" to R.raw.sfx_chirp, "tab" to R.raw.sfx_tab, "bin" to R.raw.sfx_bin,
    )

    fun load(c: Context) {
        if (pool != null) return
        // played as media, at the media volume: system sonification is muted by the tablet's silent and vibrate modes
        // (HyperOS mutes it outright), which is why the effects were never heard there
        pool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build().also { p ->
            RAW.forEach { (k, r) -> ids[k] = p.load(c.applicationContext, r, 1) }
        }
    }

    fun play(name: String, volume: Float = 1f) {
        if (on) ids[name]?.let { pool?.play(it, volume, volume, 1, 0, 1f) }
    }
}
