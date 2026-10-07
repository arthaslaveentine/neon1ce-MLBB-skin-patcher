package com.king.candycrushsaga

import java.io.File

object RootShell {
    data class Result(val exit: Int, val stdout: String, val stderr: String)

    private val SU_PATHS = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/sbin/su",
        "/su/bin/su",
        "/debug_ramdisk/su",
    )

    private fun findSu(): String {
        for (p in SU_PATHS) {
            if (File(p).exists()) return p
        }
        return "su"
    }

    fun su(command: String): Result {
        return try {
            val suPath = findSu()
            val proc = Runtime.getRuntime().exec(arrayOf(suPath, "-c", command))
            val out = proc.inputStream.bufferedReader().readText()
            val err = proc.errorStream.bufferedReader().readText()
            val code = proc.waitFor()
            Result(code, out, err)
        } catch (t: Throwable) {
            Result(-1, "", t.message ?: "unknown")
        }
    }

    fun hasRoot(): Boolean {
        val r = su("id")
        return r.exit == 0 && r.stdout.contains("uid=0")
    }
}
