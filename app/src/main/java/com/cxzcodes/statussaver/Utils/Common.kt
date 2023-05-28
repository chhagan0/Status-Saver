package com.cxzcodes.statussaver.Utils

 import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.cxzcodes.statussaver.Models.Status
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

object Common {
    val GRID_COUNT = 2
    private val CHANNEL_NAME = "GAUTHAM"
    val STATUS_DIRECTORY = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "WhatsApp/Media/.Statuses"
    )
    var APP_DIR: String? = null
    fun copyFile(status: Status, context: Context, container: RelativeLayout) {
        val file = File(APP_DIR)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Snackbar.make(container, "Something went wrong", Snackbar.LENGTH_SHORT).show()
            }
        }
        val fileName: String
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateTime = sdf.format(Date())
        if (status.isVideo()) {
            fileName = "VID_$currentDateTime.mp4"
        } else {
            fileName = "IMG_$currentDateTime.jpg"
        }
        val destFile = File(file.toString() + File.separator + fileName)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                val destinationUri: Uri?
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/status_saver"
                )
                val collectionUri: Uri
                if (status.isVideo()) {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "video/*")
                    collectionUri = MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
                    collectionUri = MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                }
                destinationUri = context.contentResolver.insert(collectionUri, values)
                val inputStream =
                    context.contentResolver.openInputStream(status.getDocumentFile().getUri())
                val outputStream = context.contentResolver.openOutputStream(
                    (destinationUri)!!
                )
                IOUtils.copy(inputStream, outputStream)
                showNotification(context, container, status, fileName, destinationUri)
            } else {
                FileUtils.copyFile(status.getFile(), destFile)
                destFile.setLastModified(System.currentTimeMillis())
                SingleMediaScanner(context, file)
                val data = FileProvider.getUriForFile(
                    context, "a.gautham.statusdownloader.provider",
                    File(destFile.absolutePath)
                )
                showNotification(context, container, status, fileName, data)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showNotification(
        context: Context, container: RelativeLayout, status: Status,
        fileName: String, data: Uri?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(context)
        }
        val intent = Intent(Intent.ACTION_VIEW)
        if (status.isVideo()) {
            intent.setDataAndType(data, "video/*")
        } else {
            intent.setDataAndType(data, "image/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val pendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_NAME)
        notification.setSmallIcon(com.cxzcodes.statussaver.R.drawable.ic_file_download_black)
            .setContentTitle(fileName)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) notification.setContentText(
            ("File Saved to " +
                    Environment.DIRECTORY_DCIM + "/status_saver")
        ) else notification.setContentText("File Saved to" + APP_DIR)
        val notificationManager: NotificationManager? =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assert(notificationManager != null)
        notificationManager!!.notify(Random().nextInt(), notification.build())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) Snackbar.make(
            container,
            "Saved to " + APP_DIR,
            Snackbar.LENGTH_LONG
        ).show() else Snackbar.make(
            container, "Saved to " + Environment.DIRECTORY_DCIM + "/status_saver",
            Snackbar.LENGTH_LONG
        ).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun makeNotificationChannel(context: Context) {
        val channel =
            NotificationChannel(CHANNEL_NAME, "Saved", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setShowBadge(true)
        val notificationManager: NotificationManager? =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assert(notificationManager != null)
        notificationManager!!.createNotificationChannel(channel)
    }
}