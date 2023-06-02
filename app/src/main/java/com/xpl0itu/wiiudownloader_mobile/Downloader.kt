package com.xpl0itu.wiiudownloader_mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.IOException

typealias ProgressCallback = (progress: Int) -> Unit

fun downloadFile(
    url: String,
    outputPath: String,
    retryCount: Int = 3,
    expectedSize: Int = 0,
    progressCallback: ((Int) -> Unit)? = null
): Boolean {
    var successful = false
    var retryAttempts = 0

    while (!successful && retryAttempts < retryCount) {
        try {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP code: ${response.code()}")
            }

            val inputStream = response.body()?.byteStream()
            val outputFile = FileOutputStream(outputPath)

            inputStream?.let {
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputFile.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (expectedSize != null) {
                        val progress = ((totalBytesRead.toDouble() / expectedSize.toDouble()) * 100).toInt()
                        progressCallback?.invoke(progress) // Notify progress through callback

                        // Update notification progress bar
                        updateNotificationProgress(progress)
                    }
                }

                outputFile.close()
                inputStream.close()
                successful = true

                // Remove the notification once download is complete
                removeNotification()
            }
        } catch (e: IOException) {
            retryAttempts++
            if (retryAttempts < retryCount) {
                println("Error occurred during file download: ${e.message}")
                println("Retrying download...")
            } else {
                println("Max retry attempts reached. File download failed.")
            }
        }
    }

    return successful
}

// Notification variables
private const val NOTIFICATION_CHANNEL_ID = "FileDownloadChannel"
private const val NOTIFICATION_ID = 12345
private lateinit var notificationManager: NotificationManager
private lateinit var notificationBuilder: NotificationCompat.Builder

// Initialize notification
fun initNotification(context: Context) {
    notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "File Download",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        channel.enableVibration(false)
        channel.setShowBadge(false)

        notificationManager.createNotificationChannel(channel)
    }

    notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        //.setSmallIcon(R.drawable.ic_download)
        .setContentTitle("File Download")
        .setProgress(100, 0, false)
        .setOngoing(true)
}

// Update notification progress bar
private fun updateNotificationProgress(progress: Int) {
    notificationBuilder.setProgress(100, progress, false)
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
}

// Remove the notification
private fun removeNotification() {
    notificationManager.cancel(NOTIFICATION_ID)
}
