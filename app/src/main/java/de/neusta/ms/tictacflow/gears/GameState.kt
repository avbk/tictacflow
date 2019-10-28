package de.neusta.ms.tictacflow.gears

data class GameState(
    val player: String,
    val field: List<String?>
) {
    companion object {
        val initial = GameState("X", List(9) { null as String? })
    }
}