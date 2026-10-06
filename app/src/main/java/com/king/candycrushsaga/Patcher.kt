package com.king.candycrushsaga

import java.io.File

object Patcher {
    private const val TERMUX_BASH = "/data/data/com.termux/files/usr/bin/bash"
    private const val PYTHON = "/data/data/com.termux/files/usr/bin/python3"
    private const val SCRIPT = "/data/data/com.termux/files/home/nyaf/nyaf_skin_patcher.py"

    data class ApplyResult(val ok: Boolean, val log: String)

    private fun runTermux(args: List<String>): RootShell.Result {
        val inner = args.joinToString(" ")
        val cmd = TERMUX_BASH + " -c '" + inner + "'"
        return RootShell.su(cmd)
    }

    fun apply(heroId: Int, skinId: Int): ApplyResult {
        val r = runTermux(listOf(PYTHON, SCRIPT, "apply", heroId.toString(), skinId.toString()))
        val log = buildString {
            append("exit=").append(r.exit).append('\n')
            if (r.stdout.isNotBlank()) append(r.stdout)
            if (r.stderr.isNotBlank()) append("\n[stderr]\n").append(r.stderr)
        }
        return ApplyResult(r.exit == 0, log)
    }

    fun revert(): ApplyResult {
        val r = runTermux(listOf(PYTHON, SCRIPT, "revert"))
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
