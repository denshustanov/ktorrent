package com.vntshsnd.ktorrent.metainfo.model

import java.math.BigInteger

class File(
    val length: BigInteger,
    val path: List<String>,
    val pathUtf8: List<String>?
)