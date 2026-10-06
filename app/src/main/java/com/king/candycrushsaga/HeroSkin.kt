package com.king.candycrushsaga

data class HeroSkin(
    val heroId: Int,
    val name: String,
    val baseSkin: Int,
    val pinyin: String?,
    val slots: List<Int>,
    val slotCount: Int,
)
