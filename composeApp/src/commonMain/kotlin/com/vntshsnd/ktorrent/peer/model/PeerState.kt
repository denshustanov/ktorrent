package com.vntshsnd.ktorrent.peer.model

enum class PeerState {
    UNAVAILABLE,
    HANDSHAKE_FAILED,
    DISCONNECTED,
    BROCKEN_PIPE,
    SEEDING,
    LEECHING,
    IDLE,
}