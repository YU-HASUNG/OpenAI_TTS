package leopardcat.studio.openaitts.data.repository

import leopardcat.studio.openaitts.data.dto.GPTChatResponse
import leopardcat.studio.openaitts.data.source.GPTChatService
import okhttp3.RequestBody
import javax.inject.Inject

class GPTChatRepository @Inject constructor(
    private val gptChatService: GPTChatService,
) {
    suspend fun getGPTChat(authorization: String, contentType: String, body: RequestBody): GPTChatResponse {
        return gptChatService.getGPTChat(authorization = authorization, contentType = contentType, body = body)
    }
}