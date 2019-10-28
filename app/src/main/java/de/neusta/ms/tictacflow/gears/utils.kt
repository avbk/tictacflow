package de.neusta.ms.tictacflow.gears

fun String.toggle() = if (this != "X") "X" else "O"

fun List<String?>.isFinished(): Boolean {
    return filterNotNull().size == 9 || getWinnerLine() != null
}

fun List<String?>.getWinnerLine(): Array<Int>? {
    val possibleSolutions = arrayOf(
        arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
        arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
        arrayOf(0, 4, 8), arrayOf(2, 4, 6)
    )
    for (solution in possibleSolutions) {
        val line = solution.map { i -> this[i] }.joinToString(separator = "")
        if (line == "XXX" || line == "OOO")
            return solution
    }

    return null
}
