package com.king.candycrushsaga

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SkinAdapter(
    private val packs: List<SkinPack>,
    private val isActive: (SkinPack) -> Boolean,
    private val onApply: (SkinPack) -> Unit,
    private val onRevert: (SkinPack) -> Unit,
) : RecyclerView.Adapter<SkinAdapter.VH>() {

    class VH(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.getChildAt(0) as TextView
        val ver: TextView = (v.getChildAt(1) as LinearLayout).getChildAt(0) as TextView
        val btn: Button = (v.getChildAt(1) as LinearLayout).getChildAt(1) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        val name = TextView(ctx).apply {
            setTextColor(Color.parseColor("#E8E8F0"))
            textSize = 16f
        }
        val subRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val ver = TextView(ctx).apply {
            setTextColor(Color.parseColor("#8A8AA0"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btn = Button(ctx).apply {
            textSize = 13f
            minWidth = 0
            minimumWidth = 0
            setPadding(32, 0, 32, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        subRow.addView(ver)
        subRow.addView(btn)
        row.addView(name)
        row.addView(subRow)
        return VH(row)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = packs[position]
        val active = isActive(p)
        holder.name.text = "Skin ${p.skinId}"
        holder.ver.text = "${p.version} · ${if (active) "ACTIVE" else "idle"}"
        holder.btn.text = if (active) "Revert" else "Apply"
        holder.btn.setOnClickListener { if (active) onRevert(p) else onApply(p) }
    }

    override fun getItemCount() = packs.size
}
