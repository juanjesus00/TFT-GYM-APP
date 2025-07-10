package geminiApi

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val error: ErrorDetails? = null
)

@Serializable
data class Candidate(
    val content: Content
)

@Serializable
data class PromptFeedback(
    val safetyRatings: List<SafetyRating>
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

@Serializable
data class ErrorDetails(
    val code: Int,
    val message: String,
    val status: String
)
