package leopardcat.studio.openaitts.data.source

import leopardcat.studio.openaitts.data.dto.GPTChatResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GPTChatService {
    @POST("completions")
    suspend fun getGPTChat(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ) : GPTChatResponse
}