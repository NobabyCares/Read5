package com.example.read5.utils.comic


/*
*
* 自然排序比较器：能正确处理 "1.jpg", "2.jpg", ..., "10.jpg"
* */
class NaturalOrderComparator {
    companion object {
        // ✅ 自然排序比较器：能正确处理 "1.jpg", "2.jpg", ..., "10.jpg"
        val naturalOrderComparator = Comparator<String> { a, b ->
            val pattern = Regex("(\\d+|\\D+)")
            val tokensA = pattern.findAll(a).map { it.value }.toList()
            val tokensB = pattern.findAll(b).map { it.value }.toList()

            val minSize = minOf(tokensA.size, tokensB.size)
            for (i in 0 until minSize) {
                val tokenA = tokensA[i]
                val tokenB = tokensB[i]
                val cmp = when {
                    tokenA.toIntOrNull() != null && tokenB.toIntOrNull() != null ->
                        tokenA.toInt().compareTo(tokenB.toInt()) // 数字按数值比
                    else ->
                        tokenA.compareTo(tokenB, ignoreCase = true) // 字符串按字母比
                }
                if (cmp != 0) return@Comparator cmp
            }
            // 如果前面都一样，短的排前面（如 "page1" vs "page1a"）
            tokensA.size.compareTo(tokensB.size)
        }
    }
}