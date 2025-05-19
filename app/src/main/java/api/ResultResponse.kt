package api

data class ResultsData(
    val reps: Int,
    val reps_durations: List<String>,
    val mean_speed: Float,
    val output_url: String

)
data class ResultResponse(
    val results: ResultsData,
    val status: String
)
