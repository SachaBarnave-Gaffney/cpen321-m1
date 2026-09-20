package com.example.cpen321application.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** The phone's local time as "hh:mm:ss GMT+hh:mm", the format M1 asks for. */
fun currentLocalTime(): String {
    val now = ZonedDateTime.now()
    val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val offset = now.offset.id // "-07:00", or "Z" when the offset is zero
    val suffix = if (offset == "Z") "+00:00" else offset
    return "$time GMT$suffix"
}
