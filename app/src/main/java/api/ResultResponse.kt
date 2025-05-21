package api

data class ResultsData(
    val mean_speed: Float,
    val reps: Int,
    val reps_durations: List<String>

)
data class ResultResponse(
    val results: ResultsData,
    val status: String
)
