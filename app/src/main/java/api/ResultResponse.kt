package api

data class ResultsData(
    val duration: Double,
    val output_url: String,
    val reps: Int
)
data class ResultResponse(
    val results: ResultsData,
    val status: String
)
