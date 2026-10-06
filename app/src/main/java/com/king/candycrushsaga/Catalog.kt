package com.king.candycrushsaga

import android.content.Context
import org.json.JSONArray
import java.io.File

data class Hero(
    val index: Int,
    val baseSkin: Int,
    val name: String,
)

data class SkinPack(
    val heroId: Int,
    val skinId: Int,
    val version: String,
    val dir: File,
)

object Catalog {
    private const val NYAF_ROOT = "/data/local/tmp/nyaf"
    private const val SKIN_PACKS = "$NYAF_ROOT/skin-packs"
    private const val CATALOG_NAME = "catalog_heroes.json"

    /** Copy the bundled catalog into filesDir on first run. */
    fun ensureCatalog(ctx: Context) {
        val f = File(ctx.filesDir, CATALOG_NAME)
        if (f.exists() && f.length() > 0) return
        try {
            ctx.assets.open(CATALOG_NAME).use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
        } catch (_: Throwable) {
            // fallback: read from /data/local/tmp if present
            val src = File("/data/local/tmp/nyaf/$CATALOG_NAME")
            if (src.exists()) src.copyTo(f, overwrite = true)
        }
    }

    fun loadHeroes(ctx: Context): List<Hero> {
        ensureCatalog(ctx)
        val f = File(ctx.filesDir, CATALOG_NAME)
        if (!f.exists()) return emptyList()
        val arr = JSONArray(f.readText())
        val out = ArrayList<Hero>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Hero(
                index = o.getInt("index"),
                baseSkin = o.getInt("base_skin"),
                name = o.getString("name"),
            ))
        }
        return out
    }

    fun scanPacks(): List<SkinPack> {
        val root = File(SKIN_PACKS)
        if (!root.isDirectory) return emptyList()
        val out = ArrayList<SkinPack>()
        root.listFiles()?.sortedBy { it.name }?.forEach { heroDir ->
            if (!heroDir.isDirectory) return@forEach
            val heroId = heroDir.name.toIntOrNull() ?: return@forEach
            heroDir.listFiles()?.sortedBy { it.name }?.forEach { skinDir ->
                if (!skinDir.isDirectory) return@forEach
                val skinId = skinDir.name.toIntOrNull() ?: return@forEach
                val verFile = File(skinDir, "pack.version")
                val ver = if (verFile.exists()) verFile.readText().trim() else "?"
                out.add(SkinPack(heroId, skinId, ver, skinDir))
            }
        }
        return out
    }

    fun packsForHero(heroId: Int): List<SkinPack> =
        scanPacks().filter { it.heroId == heroId }
}
