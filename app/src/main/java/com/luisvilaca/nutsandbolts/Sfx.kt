package com.luisvilaca.nutsandbolts

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

// Short effects for the moments that change something: a bolt driven home, a skip, a sheared bolt, a level up, a
// purchase, an undo. Synthesized by tools/sfx.py. Silent when the Sounds switch in Settings is off, and before load.
object Sfx {
    private var pool: SoundPool? = null
    private var app: Context? = null
    private val ids = mutableMapOf<String, Int>()

    fun load(c: Context) {
        if (pool != null) return
        app = c.applicationContext
        pool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build().also { p ->
            mapOf("done" to R.raw.sfx_done, "skip" to R.raw.sfx_skip, "fail" to R.raw.sfx_fail, "level" to R.raw.sfx_level, "buy" to R.raw.sfx_buy, "undo" to R.raw.sfx_undo)
                .forEach { (k, r) -> ids[k] = p.load(app, r, 1) }
        }
    }

    fun play(name: String) {
        val c = app ?: return
        if (!Store.get(c).s.sound) return
        ids[name]?.let { pool?.play(it, 1f, 1f, 1, 0, 1f) }
    }
}
