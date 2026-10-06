package com.king.candycrushsaga

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var status: TextView

    private var heroes: List<HeroData> = emptyList()
    private var allPacks: List<SkinPack> = emptyList()
    private var packCounts: MutableMap<Int, Int> = mutableMapOf()
    private var hasRoot = false
    private lateinit var adapter: HeroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasRoot = RootShell.hasRoot()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0A0A0F.toInt())
            setPadding(24, 48, 24, 24)
        }
        val title = TextView(this).apply {
            text = "Иeon1ce"
            textSize = 30f
            setTextColor(0xFF00E5FF.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "MLBB Skin Patcher"
            textSize = 12f
            setTextColor(0xFF8A8AA0.toInt())
        }
        status = TextView(this).apply {
            text = if (hasRoot) "root OK — loading..." else "root missing"
            textSize = 11f
            setTextColor(0xFF8A8AA0.toInt())
            setPadding(0, 6, 0, 12)
        }
        rv = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@MainActivity) }
        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(rv, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        lifecycleScope.launch { loadAll() }
    }

    private suspend fun loadAll() {
        val (hs, packs) = withContext(Dispatchers.IO) {
            Catalog.ensureAssets(this@MainActivity)
            val hh = Catalog.loadHeroes(this@MainActivity)
            val pp = Catalog.scanPacks().filter { it.hasFiles }
            hh to pp
        }
        heroes = hs
        allPacks = packs
        packCounts = packs.groupBy { it.heroId }.mapValues { it.value.size }.toMutableMap()
        status.text = "${heroes.size} heroes · ${packs.size} packs · " +
                if (hasRoot) "root OK" else "root missing"
        adapter = HeroAdapter(heroes, packCounts) { showHero(it) }
        rv.adapter = adapter
    }

    private fun showHero(hero: HeroData) {
        val heroPacks = allPacks.filter { it.heroId == hero.heroId }
        val packsBySlot = mutableMapOf<Int, SkinPack>()
        for (p in heroPacks) {
            val slot = extractSlotNumber(p.skinId) ?: continue
            packsBySlot[slot] = p
        }
        val current = Patcher.currentState()
        val rows = hero.skins.map { entry ->
            val pack = packsBySlot[entry.slot]
            val active = current != null && current.first == hero.heroId && current.second == pack?.skinId
            SkinRow(entry, pack, active)
        }

        val list = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        list.adapter = SkinAdapter(rows,
            onInject = { p -> runPatch(p.heroId, p.skinId) },
            onRemove = { p -> runRevert() })

        AlertDialog.Builder(this)
            .setTitle("${hero.name} — ${hero.skins.size} skins")
            .setView(list)
            .setNegativeButton("Close", null)
            .show()
    }

    private fun extractSlotNumber(skinId: Int): Int? {
        if (skinId >= 100000) return skinId % 100
        if (skinId >= 1000) return skinId % 100
        return skinId
    }

    private fun runPatch(heroId: Int, skinId: Int) {
        status.text = "injecting $heroId/$skinId..."
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Patcher.apply(heroId, skinId) }
            status.text = if (r.ok) "injected $heroId/$skinId" else "inject failed"
            Toast.makeText(this@MainActivity,
                if (r.ok) "Injected" else "Failed", Toast.LENGTH_LONG).show()
            showLog("Inject log", r.log)
        }
    }

    private fun runRevert() {
        status.text = "removing..."
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Patcher.revert() }
            status.text = if (r.ok) "removed" else "remove failed"
            showLog("Remove log", r.log)
        }
    }

    private fun showLog(t: String, b: String) {
        AlertDialog.Builder(this).setTitle(t).setMessage(b)
            .setPositiveButton("OK", null).show()
    }
}
