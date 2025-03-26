package com.vntshsnd.ktorrent.session

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class WorkerPool(private val maxWorers: Int) {
    private val jobQueue = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val workers = mutableListOf<Job>()
    private var active = true

    fun start() {
        repeat(maxWorers) {
            val worker = CoroutineScope(Dispatchers.IO).launch {
                while (active) {
                    val task = jobQueue.receive()
                    task()
                }
            }
            workers.add(worker)
        }
    }

    fun submit(task: suspend () -> Unit) = runBlocking { jobQueue.send (task) }

    fun stop() {active = false}
}