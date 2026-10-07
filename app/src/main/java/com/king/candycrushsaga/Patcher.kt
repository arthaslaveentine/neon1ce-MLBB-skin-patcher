package com.king.candycrushsaga

object Patcher {
    private const val TERMUX_SERVICE = "com.termux/com.termux.app.TermuxService"
    private const val APPLY_SCRIPT = "/data/data/com.termux/files/home/.termux/tasker/nyaf_apply.sh"
    private const val REVERT_SCRIPT = "/data/data/com.termux/files/home/.termux/tasker/nyaf_revert.sh"
    private const val LOG_PATH = "/data/data/com.termux/files/home/nyaf/patcher.log"

    data class ApplyResult(val ok: Boolean, val log: String)

    private fun runTasker(script: String, args: String?): ApplyResult {
        var cmd = "am startservice --user 0 " +
                  "-n $TERMUX_SERVICE " +
                  "-a com.termux.service_execute " +
                  "-d $script"
        if (args != null) {
            cmd += " --esa com.termux.execute.arguments \"$args\""
        }
        RootShell.su(cmd)
        Thread.sleep(8000)
        val readLog = RootShell.su("/system/bin/cat $LOG_PATH 2>/dev/null")
        val log = readLog.stdout.ifBlank { readLog.stderr }
        val ok = log.contains("[done]") && !log.contains("Traceback")
        return ApplyResult(ok, log)
    }

    fun apply(heroId: Int, skinId: Int): ApplyResult =
        runTasker(APPLY_SCRIPT, "$heroId,$skinId")

    fun revert(): ApplyResult =
        runTasker(REVERT_SCRIPT, null)

    fun currentState(): Pair<Int, Int>? {
        return try {
            val r = RootShell.su("/system/bin/cat /data/local/tmp/nyaf/skin-patcher.state")
            val lines = r.stdout.trim().lines()
            if (lines.size < 2) return null
            val parts = lines[1].split(" ")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val s = parts.getOrNull(1)?.toIntOrNull() ?: return null
            h to s
        } catch (_: Throwable) {
            null
        }
    }
}
