package com.xpl0itu.wiiudownloader_mobile

import android.content.Context
import java.io.File

fun processTitleId(
    context: Context,
    titleId: String,
    titleKey: String,
    name: String? = null,
    region: String? = null,
    outputDir: String? = null,
    retryCount: Int = 3,
    ticketsOnly: Boolean = false
) {
    val dirname = if (name != null) {
        "$titleId - $region - $name"
    } else {
        titleId
    }

    val typecheck = titleId.substring(4, 8)
    val updatedDirname = when (typecheck) {
        "000c" -> "$dirname - DLC"
        "000e" -> "$dirname - Update"
        else -> dirname
    }

    val rawdir = File("install", safeFilename(updatedDirname))

    println("Starting work in: \"$rawdir\"")

    val targetDir = if (outputDir != null) {
        File(outputDir, rawdir.path)
    } else {
        rawdir
    }

    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }

    // download TMD
    println("Downloading TMD...")

    val baseurl = "http://ccs.cdn.c.shop.nintendowifi.net/ccs/download/$titleId"
    val tmdPath = File(targetDir, "title.tmd").path

    if (!downloadFile("$baseurl/tmd", tmdPath, retryCount)) {
        println("ERROR: Could not download TMD...")
        println("MAYBE YOU ARE BLOCKING CONNECTIONS TO NINTENDO? IF YOU ARE, DON'T...! :)")
        println("Skipping title...")
        return
    }

    File(targetDir, "title.cert").writeBytes(MAGIC)

    val tmdBytes = File(tmdPath).readBytes()
    val titleVersion = tmdBytes.copyOfRange(TK + 0x9C, TK + 0x9E)

    // get ticket from keysite, from cdn if game update, or generate ticket
    if (typecheck == "000e") {
        println("\nThis is an update, so we are getting the legit ticket straight from Nintendo.")
        if (!downloadFile("$baseurl/cetk", File(targetDir, "title.tik").path, retryCount)) {
            println("ERROR: Could not download ticket from $baseurl/cetk")
            println("Skipping title...")
            return
        }
    } else {
        makeTicket(titleId, titleKey, titleVersion, File(targetDir, "title.tik"))
    }

    if (ticketsOnly) {
        println("Ticket, TMD, and CERT completed. Not downloading contents.")
        return
    }

    println("Downloading Contents...")
    val contentCount = Integer.parseInt(tmdBytes.copyOfRange(TK + 0x9E, TK + 0xA0).toHexString(), 16)

    var totalSize = 0L
    for (i in 0 until contentCount) {
        val cOffs = 0xB04 + (0x30 * i)
        totalSize += Integer.parseInt(tmdBytes.copyOfRange(cOffs + 0x08, cOffs + 0x10).toHexString(), 16)
    }
    println("Total size is ${bytesToHuman(totalSize)}\n")

    initNotification(context)

    for (i in 0 until contentCount) {
        val cOffs = 0xB04 + (0x30 * i)
        val cId = tmdBytes.copyOfRange(cOffs, cOffs + 0x04).toHexString()
        val expectedSize = Integer.parseInt(tmdBytes.copyOfRange(cOffs + 0x08, cOffs + 0x10).toHexString(), 16)
        println("\nDownloading ${i + 1} of $contentCount.")
        val outFileName = File(targetDir, "$cId.app")
        val outHashFileName = File(targetDir, "$cId.h3")

        if (!downloadFile("$baseurl/$cId", outFileName.path, retryCount, expectedSize = expectedSize)) {
            println("ERROR: Could not download content file... Skipping title")
            return
        }
        if (!downloadFile("$baseurl/$cId.h3", outHashFileName.path, retryCount)) {
            println("ERROR: Could not download h3 file... Skipping title")
            return
        }
    }

    println("\nTitle download complete in \"$dirname\"\n")
}
