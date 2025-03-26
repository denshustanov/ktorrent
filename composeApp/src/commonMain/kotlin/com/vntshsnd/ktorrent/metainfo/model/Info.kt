package com.vntshsnd.ktorrent.metainfo.model

import java.math.BigInteger

class Info(
    val hash: ByteArray,
    val name: String,
    val nameUtf8: String?,
    val pieceLength: BigInteger,
    val pieces: List<ByteArray>,
    val length: BigInteger?,
    val files: List<File>,
    )