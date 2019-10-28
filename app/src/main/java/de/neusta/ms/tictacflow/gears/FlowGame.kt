package de.neusta.ms.tictacflow.gears

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class FlowGame {

    operator fun invoke(x: Flow<Input>, o: Flow<Input>): Flow<GameState> =
        flowOf(x, o)
            .flattenMerge()
            .scan(GameState.initial, this::nextGameState)
            .distinctUntilChanged()


    private suspend fun nextGameState(current: GameState, input: Input): GameState {
        val field = current.field
        val player = current.player
        val index = input.index

        return when {
            input.player != player -> current
            field[index] != null -> current
            field.isFinished() -> current

            else -> {
                val newField = field.mapIndexed { i, f -> if (i == index) player else f }.toList()
                val newPlayer = if (newField.isFinished()) player else player.toggle()

                GameState(
                    newPlayer,
                    newField
                )
            }
        }
    }
}