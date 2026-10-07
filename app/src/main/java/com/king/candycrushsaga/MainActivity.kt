package com.king.candycrushsaga

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
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
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var status: TextView
    private lateinit var searchBox: EditText
    private lateinit var sortSpinner: Spinner

    private var allHeroes: List<HeroData> = emptyList()
    private var allPacks: List<SkinPack> = emptyList()
    private var packCounts: MutableMap<Int, Int> = mutableMapOf()
    private var hasRoot = false
    private var adapter: HeroAdapter? = null

    private val sortOptions = listOf("A to Z", "Z to A", "Skin count high to low", "Skin count low to high")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasRoot = RootShell.hasRoot()
        if (hasRoot) bootstrapWrapper()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0A0A0F.toInt())
            setPadding(24, 48, 24, 12)
        }
        val title = TextView(this).apply {
            text = "Neon1ce"
            textSize = 30f
            setTextColor(0xFF00E5FF.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "MLBB Skin Patcher"
            textSize = 12f
            setTextColor(0xFF8A8AA0.toInt())
        }
        status = TextView(this).apply {
            text = if (hasRoot) "root OK, loading" else "root missing"
            textSize = 11f
            setTextColor(0xFF8A8AA0.toInt())
            setPadding(0, 6, 0, 8)
        }
        searchBox = EditText(this).apply {
            hint = "search hero"
            setHintTextColor(0xFF5A5A70.toInt())
            setTextColor(0xFFE8E8F0.toInt())
            textSize = 14f
            setPadding(16, 12, 16, 12)
            setBackgroundColor(0xFF15151F.toInt())
        }
        sortSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item, sortOptions)
        }
        rv = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@MainActivity) }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(searchBox, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(sortSpinner, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(rv, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilter() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) { applyFilter() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lifecycleScope.launch { loadAll() }
    }

    private suspend fun loadAll() {
        val (hs, packs) = withContext(Dispatchers.IO) {
            Catalog.ensureAssets(this@MainActivity)
            val hh = Catalog.loadHeroes(this@MainActivity)
            val pp = Catalog.scanPacks().filter { it.hasFiles }
            hh to pp
        }
        allHeroes = hs
        allPacks = packs
        packCounts = packs.groupBy { it.heroId }.mapValues { it.value.size }.toMutableMap()
        status.text = allHeroes.size.toString() + " heroes, " + packs.size.toString() + " packs, " +
                (if (hasRoot) "root OK" else "root missing")
        applyFilter()
    }

    private fun applyFilter() {
        val q = searchBox.text.toString().trim().lowercase()
        var list = if (q.isEmpty()) allHeroes else allHeroes.filter {
            it.name.lowercase().contains(q)
        }
        list = when (sortSpinner.selectedItemPosition) {
            0 -> list.sortedBy { it.name.lowercase() }
            1 -> list.sortedByDescending { it.name.lowercase() }
            2 -> list.sortedByDescending { it.skins.size }
            3 -> list.sortedBy { it.skins.size }
            else -> list
        }
        adapter = HeroAdapter(list, packCounts) { showHero(it) }
        rv.adapter = adapter
    }

    private fun showHero(hero: HeroData) {
        val heroPacks = allPacks.filter { it.heroId == hero.heroId }
        val packsBySlot = mutableMapOf<Int, SkinPack>()
        for (p in heroPacks) {
            val slot = extractSlotNumber(p) ?: continue
            packsBySlot[slot] = p
        }
        val current = Patcher.currentState()
        val rows = hero.skins.map { entry ->
            val pack = packsBySlot[entry.slot]
            val active = current != null && current.first == hero.heroId && current.second == pack?.skinId
            SkinRow(entry, pack, active)
        }

        val list = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@MainActivity) }
        list.adapter = SkinAdapter(rows,
            onInject = { p -> runPatch(p.heroId, p.skinId) },
            onRemove = { p -> runRevert() })

        AlertDialog.Builder(this)
            .setTitle(hero.name + ", " + hero.skins.size.toString() + " skins")
            .setView(list)
            .setNegativeButton("Close", null)
            .show()
    }

    private fun extractSlotNumber(pack: SkinPack): Int? {
        val h = File(pack.dir, "files/Art/android/h")
        if (h.isDirectory) {
            h.listFiles()?.forEach { f ->
                val m = Regex("hero_[a-zA-Z0-9]+_skin(\\d+)_add").find(f.name)
                if (m != null) return m.groupValues[1].toIntOrNull()
            }
        }
        if (pack.version.startsWith("auto-official") && pack.skinId in 1000..9999) {
            val last2 = pack.skinId % 100
            if (last2 >= 11 && last2 < 41) return last2 - 11
        }
        return 0
    }

    private fun runPatch(heroId: Int, skinId: Int) {
        status.text = "injecting " + heroId + "/" + skinId
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Patcher.apply(heroId, skinId) }
            status.text = if (r.ok) "injected " + heroId + "/" + skinId else "inject failed"
            Toast.makeText(this@MainActivity,
                if (r.ok) "Injected" else "Failed", Toast.LENGTH_LONG).show()
            showLog("Inject log", r.log)
        }
    }

    private fun runRevert() {
        status.text = "removing"
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

    private fun bootstrapWrapper() {
        try {
            val sh = "cat > /data/local/tmp/nyaf_run.sh << 'WRAPEOF'\n" +
                "#!/data/data/com.termux/files/usr/bin/bash\n" +
                "export HOME=/data/data/com.termux/files/home\n" +
                "export PREFIX=/data/data/com.termux/files/usr\n" +
                "export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n" +
                "export TMPDIR=/data/data/com.termux/files/usr/tmp\n" +
                "export LANG=en_US.UTF-8\n" +
                "exec /data/data/com.termux/files/usr/bin/python3 /data/data/com.termux/files/home/nyaf/nyaf_skin_patcher.py \"\$@\"\n" +
                "WRAPEOF\n" +
                "chmod 755 /data/local/tmp/nyaf_run.sh"
            RootShell.su(sh)
        } catch (_: Throwable) {}
    }
}
