package com.king.candycrushsaga

import android.content.Context
import org.json.JSONArray
import java.io.File

data class SkinPack(
    val heroId: Int,
    val skinId: Int,
    val version: String,
    val dir: File,
    val hasFiles: Boolean,
)

data class SkinEntry(
    val slot: Int,
    val name: String?,
    val tier: String?,
    val tierColor: String?,
    val availability: String?,
)

data class HeroData(
    val heroId: Int,
    val name: String,
    val baseSkin: Int,
    val skins: List<SkinEntry>,
)

object Catalog {
    private const val NYAF_ROOT = "/data/local/tmp/nyaf"
    private const val SKIN_PACKS = "$NYAF_ROOT/skin-packs"
    private const val FULL_CATALOG = "catalog_full.json"

    fun ensureAssets(ctx: Context) {
        val f = File(ctx.filesDir, FULL_CATALOG)
        if (f.exists() && f.length() > 0) return
        try {
            ctx.assets.open(FULL_CATALOG).use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
        } catch (_: Throwable) {}
    }

    fun loadHeroes(ctx: Context): List<HeroData> {
        ensureAssets(ctx)
        val f = File(ctx.filesDir, FULL_CATALOG)
        if (!f.exists()) return emptyList()
        val arr = JSONArray(f.readText())
        val out = ArrayList<HeroData>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val skinArr = o.optJSONArray("skins") ?: JSONArray()
            val skins = ArrayList<SkinEntry>(skinArr.length())
            for (j in 0 until skinArr.length()) {
                val s = skinArr.getJSONObject(j)
                skins.add(SkinEntry(
                    slot = s.optInt("slot", 0),
                    name = s.optString("name", null)?.takeIf { it != "null" && it.isNotBlank() },
                    tier = s.optString("tier", null)?.takeIf { it != "null" && it.isNotBlank() },
                    tierColor = s.optString("tier_color", null)?.takeIf { it != "null" && it.isNotBlank() },
                    availability = s.optString("availability", null)?.takeIf { it != "null" && it.isNotBlank() },
                ))
            }
            out.add(HeroData(
                heroId = o.getInt("hero_id"),
                name = o.getString("name"),
                baseSkin = o.getInt("base_skin"),
                skins = skins,
            ))
        }
        return out
    }

    fun scanPacks(): List<SkinPack> {
        val root = File(SKIN_PACKS)
        if (!root.isDirectory) return emptyList()
        val out = ArrayList<SkinPack>()
        root.listFiles()?.forEach { heroDir ->
            if (!heroDir.isDirectory) return@forEach
            val heroId = heroDir.name.toIntOrNull() ?: return@forEach
            heroDir.listFiles()?.forEach { skinDir ->
                if (!skinDir.isDirectory) return@forEach
                val skinId = skinDir.name.toIntOrNull() ?: return@forEach
                val verFile = File(skinDir, "pack.version")
                val ver = if (verFile.exists()) verFile.readText().trim() else "?"
                val filesDir = File(skinDir, "files")
                val hasFiles = filesDir.isDirectory && (filesDir.listFiles()?.isNotEmpty() == true)
                out.add(SkinPack(heroId, skinId, ver, skinDir, hasFiles))
            }
        }
        return out
    }

    fun packsForHero(heroId: Int): List<SkinPack> =
        scanPacks().filter { it.heroId == heroId && it.hasFiles }
}
