package leopardcat.studio.openaitts.data.repository

import leopardcat.studio.openaitts.data.source.GPTAudioService
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class GPTAudioRepository @Inject constructor(
    private val gptAudioService: GPTAudioService,
) {
    suspend fun getGPTAudio(authorization: String, contentType: String, body: RequestBody): ResponseBody {
        return gptAudioService.getGPTAudio(authorization = authorization, contentType = contentType, body = body)
    }
}