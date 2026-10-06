package com.king.candycrushsaga

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeroAdapter(
    private val ctx: Context,
    private val heroes: List<Hero>,
    private val packCounts: Map<Int, Int>,
    private val onClick: (Hero) -> Unit,
) : RecyclerView.Adapter<HeroAdapter.VH>() {

    class VH(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.getChildAt(0) as ImageView
        val name: TextView = v.getChildAt(1) as TextView
        val count: TextView = v.getChildAt(2) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val cell = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(12, 12, 12, 12)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        val img = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(240, 240).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.parseColor("#15151F"))
        }
        val name = TextView(ctx).apply {
            setTextColor(Color.parseColor("#E8E8F0"))
            textSize = 13f
            gravity = Gravity.CENTER
            maxLines = 2
            setPadding(0, 8, 0, 0)
        }
        val count = TextView(ctx).apply {
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxLines = 1
        }
        cell.addView(img)
        cell.addView(name)
        cell.addView(count)
        return VH(cell)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = heroes[position]
        holder.name.text = h.name
        val c = packCounts[h.index] ?: 0
        holder.count.text = if (c > 0) "$c pack${if (c > 1) "s" else ""}" else "—"
        holder.count.setTextColor(if (c > 0) 0xFF00E5FF.toInt() else 0xFF5A5A70.toInt())
        // load icon from assets
        try {
            ctx.assets.open("heroes/${h.index}.png").use { input ->
                val bmp = android.graphics.BitmapFactory.decodeStream(input)
                holder.img.setImageBitmap(bmp)
            }
        } catch (_: Throwable) {
            holder.img.setImageDrawable(null)
        }
        holder.itemView.setOnClickListener { onClick(h) }
    }

    override fun getItemCount() = heroes.size
}
