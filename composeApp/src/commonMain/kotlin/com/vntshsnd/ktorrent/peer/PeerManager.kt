package com.vntshsnd.ktorrent.peer

import com.vntshsnd.ktorrent.peer.model.PeerState
import com.vntshsnd.ktorrent.piece.PieceManager
import com.vntshsnd.ktorrent.session.WorkerPool
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import com.vntshsnd.ktorrent.tracker.TrackerService
import org.slf4j.LoggerFactory

class PeerManager(val torrentMetaInfo: TorrentMetaInfo, val peerId: ByteArray, val pieceManager: PieceManager) {
    companion object {
        val log = LoggerFactory.getLogger(PeerManager::class.java)
    }

    val allPeers = mutableListOf<Peer>()
    var availablePeers = mutableListOf<Peer>()
    private val trackerService = TrackerService.instance

    private var peerWorkerPool: WorkerPool? = null

    fun discoverPeers() {
        val peersConnectionData = trackerService.queryPeers(torrentMetaInfo, peerId)
        allPeers.addAll(peersConnectionData.map { Peer(it.host, it.port, torrentMetaInfo, peerId, pieceManager) })
        log.info("Discovered ${allPeers.size} peers")
        peerWorkerPool = WorkerPool(allPeers.size)
    }

    fun connectToPeers() {
        peerWorkerPool?.start()
        for(peer in allPeers) {
            peerWorkerPool?.submit { peer.peerInteractionLoop() }
        }
        log.info("Got ${allPeers.filter { it.state != PeerState.DISCONNECTED || it.state != PeerState.UNAVAILABLE }.size} available peers")
    }

    fun stopPeers() {
        for (peer in availablePeers) {
            peer.stop()
        }
        peerWorkerPool?.stop()
    }
}