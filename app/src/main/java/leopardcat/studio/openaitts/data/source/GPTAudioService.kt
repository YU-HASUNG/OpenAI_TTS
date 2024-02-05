package leopardcat.studio.openaitts.data.source

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GPTAudioService {
    @POST("speech")
    suspend fun getGPTAudio(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): ResponseBody
}