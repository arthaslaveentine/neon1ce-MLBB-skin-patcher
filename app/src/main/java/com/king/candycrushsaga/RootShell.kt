package com.king.candycrushsaga

import java.io.DataOutputStream

object RootShell {
    data class Result(val exit: Int, val stdout: String, val stderr: String)

    fun run(vararg cmd: String): Result {
        return try {
            val proc = ProcessBuilder(*cmd)
                .redirectErrorStream(false)
                .start()
            val out = proc.inputStream.bufferedReader().readText()
            val err = proc.errorStream.bufferedReader().readText()
            val code = proc.waitFor()
            Result(code, out, err)
        } catch (t: Throwable) {
            Result(-1, "", t.message ?: "unknown")
        }
    }

    /** Run a single command as root via `su -c`. */
    fun su(command: String): Result {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
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
