package api

data class AnalyzeResponse(
    val analysis_id : String,
    val message: String,
    val status: String
)
