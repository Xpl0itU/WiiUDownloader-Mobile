package com.xpl0itu.wiiudownloader_mobile

import java.io.Serializable

object gtitlesWrapper {
    init {
        System.loadLibrary("gtitles")
    }

    data class TitleEntry(
        val name: String,
        val tid: Long,
        val region: Int,
        val key: Int
    ) : Serializable

    external fun getTitleEntries(cat: Int): Array<TitleEntry>
    external fun getTitleEntriesSize(cat: Int): Long
}