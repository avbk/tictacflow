package de.neusta.ms.tictacflow.gears

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
class Websocket {
    @FlowPreview
    suspend fun connect(addr: String): Client {
        return suspendCoroutine { continuation ->
            lateinit var client: Client
            val channel = Channel<Data>()

            val realClient = object : WebSocketClient(URI(addr)) {
                private var resumed = false

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    channel.offer(Data.Close(code, reason, remote))
                    channel.close()
                }

                override fun onMessage(message: String?) {
                    channel.offer(Data.Message(message))
                }

                override fun onOpen(handshakedata: ServerHandshake) {
                    if (!resumed) {
                        resumed = true
                        continuation.resume(client)
                    }
                }

                override fun onError(ex: Exception) {
                    if (!resumed) {
                        resumed = true
                        channel.close()
                        continuation.resumeWithException(ex)
                    }
                }
            }

            channel.invokeOnClose { realClient.close() }
            client = Client(realClient, channel.consumeAsFlow())
            realClient.connect()
        }


    }

    sealed class Data {
        data class Close(
            val code: Int,
            val reason: String?,
            val remote: Boolean
        ) : Data()

        data class Message(
            val messsage: String?
        ) : Data()
    }


    class Client(
        private val realClient: WebSocketClient,
        val incoming: Flow<Data>
    ) {

        fun send(messsage: String) {
            realClient.send(messsage)
        }

        suspend fun close() {
            return suspendCoroutine {
                realClient.closeBlocking()
                it.resume(Unit)
            }
        }

    }

}