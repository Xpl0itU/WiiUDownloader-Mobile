package com.xpl0itu.wiiudownloader_mobile

import java.io.IOException
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

const val HEX_CHARS = "0123456789abcdef"

fun String.isHex(evenLength: Boolean = false): Boolean {
    if (isBlank()) {
        return false
    }
    if (evenLength && length % 2 != 0) {
        // must be even, 1-byte = 2-hex-chars
        return false
    }

    return this.all { char -> HEX_CHARS.contains(char.lowercaseChar()) }
}

fun String.hexToByteArray(): ByteArray {
    if (this.length % 2 != 0) {
        throw IOException("String length must be even, 1-byte = 2-hex-chars")
    }
    if (!this.isHex()) {
        throw IOException("Invalid Hex String")
    }

    val hex = lowercase(Locale.ROOT)
    val result = ByteArray(length / 2)

    for (index in hex.indices step 2) {
        val firstChar = HEX_CHARS.indexOf(hex[index])
        val secondChar = HEX_CHARS.indexOf(hex[index + 1])

        val octet = firstChar.shl(4) + secondChar
        result[index / 2] = octet.toByte()
    }

    return result
}

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}

fun bytesToHuman(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return "%.1f %s".format(bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

fun safeFilename(filename: String): String {
    return filename.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
}
