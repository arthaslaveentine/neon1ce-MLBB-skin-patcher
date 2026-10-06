package com.king.candycrushsaga

import android.content.Context
import org.json.JSONArray
import java.io.File

data class SkinPack(
    val heroId: Int,
    val skinId: Int,
    val version: String,
    val dir: File,
)

object Catalog {
    private const val NYAF_ROOT = "/data/local/tmp/nyaf"
    private const val SKIN_PACKS = "$NYAF_ROOT/skin-packs"
    private const val CATALOG = "catalog_heroes.json"
    private const val HERO_SKINS = "hero_skins.json"

    fun ensureAssets(ctx: Context) {
        for (name in listOf(CATALOG, HERO_SKINS)) {
            val f = File(ctx.filesDir, name)
            if (f.exists() && f.length() > 0) continue
            try {
                ctx.assets.open(name).use { input ->
                    f.outputStream().use { input.copyTo(it) }
                }
            } catch (_: Throwable) {}
        }
    }

    fun loadHeroSkins(ctx: Context): List<HeroSkin> {
        ensureAssets(ctx)
        val f = File(ctx.filesDir, HERO_SKINS)
        if (!f.exists()) return emptyList()
        val arr = JSONArray(f.readText())
        val out = ArrayList<HeroSkin>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val slotsArr = o.optJSONArray("slots")
            val slots = ArrayList<Int>()
            if (slotsArr != null) {
                for (j in 0 until slotsArr.length()) slots.add(slotsArr.getInt(j))
            }
            out.add(HeroSkin(
                heroId = o.getInt("hero_id"),
                name = o.getString("name"),
                baseSkin = o.getInt("base_skin"),
                pinyin = o.optString("pinyin", null),
                slots = slots,
                slotCount = o.optInt("slot_count", slots.size),
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
                out.add(SkinPack(heroId, skinId, ver, skinDir))
            }
        }
        return out
    }

    fun packsForHero(heroId: Int): List<SkinPack> =
        scanPacks().filter { it.heroId == heroId }
}
