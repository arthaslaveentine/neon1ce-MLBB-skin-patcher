package com.king.candycrushsaga

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeroAdapter(
    private val heroes: List<Hero>,
    private val packCounts: Map<Int, Int>,
    private val onClick: (Hero) -> Unit,
) : RecyclerView.Adapter<HeroAdapter.VH>() {

    class VH(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.getChildAt(0) as TextView
        val sub: TextView = v.getChildAt(1) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        val name = TextView(ctx).apply {
            setTextColor(Color.parseColor("#E8E8F0"))
            textSize = 17f
        }
        val sub = TextView(ctx).apply {
            setTextColor(Color.parseColor("#8A8AA0"))
            textSize = 12f
        }
        row.addView(name)
        row.addView(sub)
        return VH(row)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = heroes[position]
        holder.name.text = h.name
        val c = packCounts[h.index] ?: 0
        holder.sub.text = "base skin ${h.baseSkin} · $c pack" + (if (c == 1) "" else "s")
        holder.itemView.setOnClickListener { onClick(h) }
    }

    override fun getItemCount() = heroes.size
}
