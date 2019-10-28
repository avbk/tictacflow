package de.neusta.ms.tictacflow.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.neusta.ms.tictacflow.R
import de.neusta.ms.tictacflow.databinding.FragmentGameBinding
import de.neusta.ms.tictacflow.gears.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.IOException


@FlowPreview
@ExperimentalCoroutinesApi
class FlowGameFragment : Fragment() {

    enum class GameType {
        OFFLINE,
        ONLINE_X,
        ONLINE_O
    }

    lateinit var binding: FragmentGameBinding

    private val buttons by lazy {
        listOf(
            binding.field0,
            binding.field1,
            binding.field2,
            binding.field3,
            binding.field4,
            binding.field5,
            binding.field6,
            binding.field7,
            binding.field8
        )
    }

    private val info by lazy { binding.info }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false)
        return binding.root
    }

    private var currentPlayer: String? = null
    private var gameType = GameType.OFFLINE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gameType = GameType.values()[arguments?.getInt("GAME_TYPE") ?: 0]

        val onClick = callbackFlow {
            buttons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    offer(index)
                }
            }
            awaitClose {
                buttons.forEachIndexed { i, button -> button.setOnClickListener(null) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val (x, o) = getPlayerStreams(onClick)

            val game = FlowGame()
            game(x, o).collect { state ->
                currentPlayer = state.player
                renderField(state.field)
                renderInfo(state)

                val line = state.field.getWinnerLine()
                if (line != null)
                    renderWinnerLine(line)
            }
        }
    }

    private fun renderField(field: List<String?>) {
        buttons.forEachIndexed { i, button ->
            button.text = field[i] ?: ""
        }
    }

    private fun renderInfo(state: GameState) {
        val isFinished = state.field.isFinished()
        val winnerLine = state.field.getWinnerLine()
        val player = state.player

        val message = if (isFinished && winnerLine != null)
            "Spieler $player hat gewonnen"
        else if (isFinished)
            "Untentschieden"
        else
            "Spieler $player ist an der Reihe"

        info.text = message
    }

    private fun renderWinnerLine(winnerLine: Array<Int>) {
        winnerLine
            .map { index -> buttons[index] }
            .forEach { button ->
                button.setTextColor(Color.WHITE)
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF00574B"))
            }
    }

    private suspend fun CoroutineScope.getPlayerStreams(onClick: Flow<Int>): Players {
        if (gameType == GameType.OFFLINE) {
            val onClickShared = onClick
                .map { Input(currentPlayer ?: "X", it) }
                .broadcastIn(this)
                .asFlow()

            return Players(
                onClickShared.filter { it.player == "X" },
                onClickShared.filter { it.player == "O" }
            )
        } else if (gameType == GameType.ONLINE_X) {
            val client = createWS()

            return Players(
                onClick.map { Input("X", it) }.dispatchToWS(client),
                client?.incoming.receiveAs("O")
            )
        } else {
            val client = createWS()

            return Players(
                client?.incoming.receiveAs("X"),
                onClick.map { Input("O", it) }.dispatchToWS(client)
            )
        }
    }

    private suspend fun createWS() =
        try {
            Websocket().connect("ws://10.0.2.2:8000")
        } catch (e: IOException) {
            null
        }


    private fun Flow<Websocket.Data>?.receiveAs(player: String) = this
        ?.mapNotNull { it as? Websocket.Data.Message }
        ?.mapNotNull { it.messsage }
        ?.mapNotNull { it.toIntOrNull() }
        ?.filter { it in 0..8 }
        ?.map { Input(player, it) }
        ?: emptyFlow()


    private fun Flow<Input>.dispatchToWS(client: Websocket.Client?) = this
        .onEach {
            if (client == null)
                throw IOException("Client not connected")
            client.send(it.index.toString())
        }
        .catch { emit(Input("E", -1)) }
        .flowOn(Dispatchers.IO)

    private data class Players(
        val x: Flow<Input>,
        val o: Flow<Input>
    )
}
