package com.monkey.mediatimer.utils

import android.content.Context

fun Context.getStringResourceById(id: Int): String {
    return this.resources.getString(id)
}