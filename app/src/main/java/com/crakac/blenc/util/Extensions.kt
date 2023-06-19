package com.crakac.blenc.util

import kotlin.time.Duration
import kotlin.time.DurationUnit

fun ByteArray.toHex(): String {
    return "0x" + map { "%02X".format(it) }
}

val Duration.millis: Int
    get() {
        return toInt(DurationUnit.MILLISECONDS)
    }