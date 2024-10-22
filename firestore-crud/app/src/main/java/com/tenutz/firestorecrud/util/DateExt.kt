package com.tenutz.firestorecrud.util

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

val now get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
} else {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}

val Long.formattedDate get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
} else {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(this))
}