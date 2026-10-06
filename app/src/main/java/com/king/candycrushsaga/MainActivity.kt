package com.king.candycrushsaga

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var status: TextView

    private var heroes: List<Hero> = emptyList()
    private var packCounts: MutableMap<Int, Int> = mutableMapOf()
    private var hasRoot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasRoot = RootShell.hasRoot()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0A0A0F.toInt())
            setPadding(0, 48, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Иeon1ce"
            textSize = 30f
            setTextColor(0xFF00E5FF.toInt())
            setPadding(24, 0, 24, 0)
        }
        val subtitle = TextView(this).apply {
            text = "MLBB Skin Patcher"
            textSize = 12f
            setTextColor(0xFF8A8AA0.toInt())
            setPadding(24, 2, 24, 0)
        }
        status = TextView(this).apply {
            text = if (hasRoot) "root OK — loading..." else "root missing"
            textSize = 11f
            setTextColor(0xFF8A8AA0.toInt())
            setPadding(24, 6, 24, 12)
        }

        rv = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            setPadding(8, 0, 8, 24)
            clipToPadding = false
        }
        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(rv, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)

        lifecycleScope.launch { loadHeroes() }
    }

    private suspend fun loadHeroes() {
        val loaded = withContext(Dispatchers.IO) {
            Catalog.ensureCatalog(this@MainActivity)
            val hs = Catalog.loadHeroes(this@MainActivity)
            val packs = Catalog.scanPacks()
            val counts = packs.groupBy { it.heroId }.mapValues { it.value.size }
            hs to counts
        }
        heroes = loaded.first
        packCounts = loaded.second.toMutableMap()
        status.text = "${heroes.size} heroes · ${packCounts.values.sum()} packs · " +
                if (hasRoot) "root OK" else "root missing"

        rv.adapter = HeroAdapter(this, heroes, packCounts) { hero ->
            showSkinPicker(hero)
        }
    }

    private fun showSkinPicker(hero: Hero) {
        val packs = Catalog.packsForHero(hero.index)
        if (packs.isEmpty()) {
            Toast.makeText(this, "No packs for ${hero.name}", Toast.LENGTH_SHORT).show()
            return
        }
        val current = Patcher.currentState()

        val list = RecyclerView(this).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
        }
        val adapter = SkinAdapter(
            packs,
            isActive = { p -> current != null && current.first == p.heroId && current.second == p.skinId },
            onApply = { p -> runPatch(p.heroId, p.skinId) },
            onRevert = { _ -> runRevert() },
        )
        list.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("${hero.name} — skins")
            .setView(list)
            .setNegativeButton("Close", null)
            .show()
    }

    private fun runPatch(heroId: Int, skinId: Int) {
        status.text = "applying $heroId/$skinId..."
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Patcher.apply(heroId, skinId) }
            status.text = if (r.ok) "applied $heroId/$skinId" else "apply failed"
            Toast.makeText(this@MainActivity,
                if (r.ok) "Applied" else "Failed — see log", Toast.LENGTH_LONG).show()
            showLogDialog("Patcher output", r.log)
        }
    }

    private fun runRevert() {
        status.text = "reverting..."
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Patcher.revert() }
            status.text = if (r.ok) "reverted" else "revert failed"
            showLogDialog("Revert output", r.log)
        }
    }

    private fun showLogDialog(title: String, body: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton("OK", null)
            .show()
    }
}
