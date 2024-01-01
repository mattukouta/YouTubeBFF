package com.kouta.youtubebff.vo.enums

enum class Sort(val value: String) {
    /**　更新日の昇順　*/
    UPDATED_AT_ASC("updatedAtAsc"),
    /**　更新日の降順　*/
    UPDATED_AT_DESC("updatedAtDesc");

    companion object {
        fun findByValue(value: String): Sort? = Sort.entries.firstOrNull { it.value == value }
    }
}