package com.vntshsnd.ktorrent

import java.security.MessageDigest

fun ByteArray.encodeUrlString(): String {
    val ret = StringBuilder()
    for (b in this) {
        val char = b.toInt().toChar()
        if (char == ' ') {
            ret.append("+")
        } else if ("-._~".contains(char)) {
            ret.append(char)
        } else ret.append("%%%02X".format(b))
    }

    return ret.toString()
}

fun ByteArray.sha1() = MessageDigest.getInstance("SHA-1").digest(this)