package com.king.candycrushsaga

import java.io.File

object Patcher {
    private const val WRAPPER = "/data/local/tmp/nyaf_run.sh"
    private const val SH = "/system/bin/sh"

    data class ApplyResult(val ok: Boolean, val log: String)

    fun apply(heroId: Int, skinId: Int): ApplyResult {
        val cmd = "$SH $WRAPPER apply $heroId $skinId"
        val r = RootShell.su(cmd)
        val log = buildString {
            append("exit=").append(r.exit).append('\n')
            if (r.stdout.isNotBlank()) append(r.stdout)
            if (r.stderr.isNotBlank()) append("\n[stderr]\n").append(r.stderr)
        }
        return ApplyResult(r.exit == 0, log)
    }

    fun revert(): ApplyResult {
        val r = RootShell.su("$SH $WRAPPER revert")
        return ApplyResult(r.exit == 0, r.stdout + r.stderr)
    }

    fun currentState(): Pair<Int, Int>? {
        return try {
            val f = File("/data/local/tmp/nyaf/skin-patcher.state")
            if (!f.exists() || !f.canRead()) return null
            val lines = f.readText().trim().lines()
            if (lines.size < 2) return null
            val parts = lines[1].split(" ")
            if (parts.size < 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val s = parts[1].toIntOrNull() ?: return null
            h to s
        } catch (_: Throwable) {
            null
        }
    }
}
