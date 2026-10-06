package com.king.candycrushsaga

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SkinRow(
    val entry: SkinEntry,
    val pack: SkinPack?,
    val isActive: Boolean,
)

class SkinAdapter(
    private val rows: List<SkinRow>,
    private val onInject: (SkinPack) -> Unit,
    private val onRemove: (SkinPack) -> Unit,
) : RecyclerView.Adapter<SkinAdapter.VH>() {

    class VH(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.getChildAt(0) as TextView
        val tier: TextView = v.getChildAt(1) as TextView
        val row: LinearLayout = v.getChildAt(2) as LinearLayout
        val status: TextView = row.getChildAt(0) as TextView
        val btn: Button = row.getChildAt(1) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val cell = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 20, 28, 20)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        val name = TextView(ctx).apply {
            setTextColor(Color.parseColor("#E8E8F0"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }
        val tier = TextView(ctx).apply {
            textSize = 12f
            setPadding(0, 3, 0, 4)
        }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val status = TextView(ctx).apply {
            setTextColor(Color.parseColor("#8A8AA0"))
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btn = Button(ctx).apply {
            textSize = 11f
            minWidth = 0
            minimumWidth = 0
            setPadding(24, 0, 24, 0)
        }
        row.addView(status)
        row.addView(btn)
        cell.addView(name)
        cell.addView(tier)
        cell.addView(row)
        return VH(cell)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = rows[position]
        val e = r.entry
        val displayName = e.name ?: if (e.slot == 0) "Base" else "Skin %02d".format(e.slot)
        holder.name.text = "%02d  %s".format(e.slot, displayName)

        // tier label
        if (e.tier.isNullOrBlank()) {
            holder.tier.text = "—"
            holder.tier.setTextColor(0xFF8A8AA0.toInt())
        } else {
            holder.tier.text = e.tier
            val colorStr = e.tierColor ?: "9E9E9E"
            val c = try { Color.parseColor("#" + colorStr) } catch (_: Throwable) { 0xFF9E9E9E.toInt() }
            holder.tier.setTextColor(c)
        }

        // status + button
        if (r.pack == null || !r.pack.hasFiles) {
            holder.status.text = if (e.availability == "Limited") "not installed · limited" else "not installed"
            holder.status.setTextColor(0xFF5A5A70.toInt())
            holder.btn.visibility = Button.GONE
        } else {
            if (r.isActive) {
                holder.status.text = "ACTIVE"
                holder.status.setTextColor(0xFF00E5FF.toInt())
                holder.btn.text = "REMOVE"
                holder.btn.visibility = Button.VISIBLE
                holder.btn.setOnClickListener { onRemove(r.pack) }
            } else {
                holder.status.text = "ready · ${r.pack.version}"
                holder.status.setTextColor(0xFFFF2E88.toInt())
                holder.btn.text = "INJECT"
                holder.btn.visibility = Button.VISIBLE
                holder.btn.setOnClickListener { onInject(r.pack) }
            }
        }
    }

    override fun getItemCount() = rows.size
}
