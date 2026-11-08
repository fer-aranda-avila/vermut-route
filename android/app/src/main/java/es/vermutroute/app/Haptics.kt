package es.vermutroute.app

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** The three haptic languages, as in the design canvas and BRIEFING.md. */
enum class Lang(val key: String, val title: String, val desc: String) {
    A("A", "Frío / caliente", "Late cuando vas en la dirección buena"),
    B("B", "Hacia dónde girar", "Te avisa si el bar queda a tu izquierda, a tu derecha o detrás"),
    C("C", "Radar", "Late más rápido cuanto más cerca estás"),
}

/** Patterns in ms, alternating vibration and pause, starting with a vibration. */
object Patterns {
    val ARRIVE = longArrayOf(500, 120, 150, 120, 500)

    // A · frío / caliente
    val A_OK = longArrayOf(60, 120, 60)
    val A_BACK = longArrayOf(300)
    val A_NEAR = longArrayOf(60, 80, 60, 80, 60)

    // B · hacia dónde girar
    val B_LEFT = longArrayOf(120, 150, 120)
    val B_RIGHT = longArrayOf(450)
    val B_UTURN = longArrayOf(80, 80, 80, 80, 80, 80, 80, 80, 80)

    // C · radar
    val C_BEAT = longArrayOf(80)
    val C_AWAY = longArrayOf(700)
}

data class Signal(val name: String, val desc: String, val pattern: LongArray, val everyMs: Long? = null)

val SIGNALS: Map<Lang, List<Signal>> = mapOf(
    Lang.A to listOf(
        Signal("Vas bien", "Doble tic cada 3 s", Patterns.A_OK, 3000),
        Signal("Vuelves al rumbo", "Un toque largo", Patterns.A_BACK),
        Signal("A menos de 100 m", "Tic-tic-tic cada segundo", Patterns.A_NEAR, 1000),
        Signal("Has llegado", "Largo · corto · largo", Patterns.ARRIVE),
    ),
    Lang.B to listOf(
        Signal("Gira a la izquierda", "Dos toques cortos", Patterns.B_LEFT),
        Signal("Gira a la derecha", "Un toque largo", Patterns.B_RIGHT),
        Signal("Media vuelta", "Ráfaga de toques", Patterns.B_UTURN),
        Signal("Has llegado", "Largo · corto · largo", Patterns.ARRIVE),
    ),
    Lang.C to listOf(
        Signal("Lejos · más de 300 m", "Un latido cada 5 s", Patterns.C_BEAT, 5000),
        Signal("Cerca · 100–300 m", "Un latido cada 2 s", Patterns.C_BEAT, 2000),
        Signal("Casi · menos de 100 m", "Un latido cada 0,7 s", Patterns.C_BEAT, 700),
        Signal("Te alejas", "Zumbido largo", Patterns.C_AWAY),
        Signal("Has llegado", "Largo · corto · largo", Patterns.ARRIVE),
    ),
)

object Haptics {
    private fun vibrator(ctx: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= 31) {
            ctx.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Vibrator::class.java)
        }

    /**
     * Vibrates with "alarm" usage: unlike a web page, a native app can say what
     * its vibration is for, so it does not depend on the touch-feedback setting.
     */
    fun play(ctx: Context, pattern: LongArray) {
        if (pattern.isEmpty()) return
        val effect = VibrationEffect.createWaveform(longArrayOf(0L) + pattern, -1)
        val v = vibrator(ctx)
        if (Build.VERSION.SDK_INT >= 33) {
            v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
        }
    }

    /** A signal repeated at its real rhythm (for the test screen). */
    fun preview(ctx: Context, s: Signal) {
        val every = s.everyMs
        if (every == null) {
            play(ctx, s.pattern)
            return
        }
        val n = (6000 / every + 1).toInt().coerceIn(2, 6)
        val gap = (every - s.pattern.sum()).coerceAtLeast(0)
        val out = mutableListOf<Long>()
        repeat(n) { i ->
            out += s.pattern.toList()
            if (i < n - 1) out += gap
        }
        play(ctx, out.toLongArray())
    }

    fun cancel(ctx: Context) = vibrator(ctx).cancel()
}
