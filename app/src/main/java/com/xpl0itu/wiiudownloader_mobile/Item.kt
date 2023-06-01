package com.xpl0itu.wiiudownloader_mobile

import java.io.Serializable

data class Item(
    val titleID: String,
    val ticket: Int,
    val titleKey: String,
    val name: String,
    val region: String
) : Serializable
