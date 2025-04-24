package com.monkey.mediatimer.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils.equals
import android.text.TextUtils.isEmpty
import com.monkey.mediatimer.common.MediaInfo
import java.util.concurrent.TimeUnit

fun formatTimeDisplay(durationMs: Long): String {
    // todo checking format for hour
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}

fun getAppNameFromPackage(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName // fallback nếu không tìm được
    }
}

fun MutableList<MediaInfo>.updateOrAddItem(item: MediaInfo) {
    val index = this.indexOfFirst { it.packageName == item.packageName }
    if (index != -1) {
        this[index] = item
    }
    else this.add(item)
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    /*val pkgName = context.packageName
    val enabledListeners = android.provider.Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false

    return enabledListeners.contains(pkgName)*/
    val packageName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    if (!isEmpty(flat)) {
        val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in names.indices) {
            val cn = ComponentName.unflattenFromString(names[i])
            if (cn != null && equals(packageName, cn.packageName)) {
                return true
            }
        }
    }
    return false
}

fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}