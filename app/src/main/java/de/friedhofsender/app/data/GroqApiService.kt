package de.friedhofsender.app.data
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
interface GroqApiService {
    @POST("openai/v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun createChatCompletion(
        @Body request: GroqChatRequest
    ): GroqChatResponse
}
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.8,
    val max_tokens: Int = 2048
)
data class GroqMessage(
    val role: String,
    val content: String
)
data class GroqChatResponse(
    val choices: List<GroqChoice>
)
data class GroqChoice(
    val message: GroqMessage
)