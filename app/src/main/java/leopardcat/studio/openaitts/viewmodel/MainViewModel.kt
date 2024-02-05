package leopardcat.studio.openaitts.viewmodel

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leopardcat.studio.openaitts.data.dto.GPTAudioRequest
import leopardcat.studio.openaitts.data.repository.GPTAudioRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Named

@HiltViewModel
class MainViewModel @javax.inject.Inject constructor(
    private val gptAudioRepository: GPTAudioRepository, //Audio
    @Named("ApiKey")
    private val apiKey: String, //apiKey
) : ViewModel() {

    //미디어 플레이어
    private var mediaPlayer: MediaPlayer? = null

    //오디오 다운로드 및 재생
    fun makeAudio(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val destinationFile = File(context.getExternalFilesDir(null), "speech.mp3")
            downloadAudioFile(destinationFile)
        }
    }

    private suspend fun downloadAudioFile(destination: File) {
        val audioResponse = gptAudioRepository.getGPTAudio(
            apiKey,
            "application/json",
            createGPTAudioRequestBody(
                GPTAudioRequest(
                    "tts-1",
                    "HI Let's start TTS Test",
                    "alloy",
                    "aac",
                    0.6f
                )
            )
        )

        withContext(Dispatchers.IO) {
            val inputStream: InputStream = audioResponse.byteStream()
            val outputStream = FileOutputStream(destination)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead) //파일 저장
            }
            outputStream.close()
        }

        playAudioFile(destination)
    }

    //RequestBody 제작
    private fun createGPTAudioRequestBody(gptAudioRequest: GPTAudioRequest): RequestBody {
        val json = JSONObject().apply {
            put("model", gptAudioRequest.model)
            put("input", gptAudioRequest.input)
            put("voice", gptAudioRequest.voice)
            put("response_format", gptAudioRequest.response_format)
            put("speed", gptAudioRequest.speed)
        }

        return json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    //오디오 재생
    private fun playAudioFile(audioFile: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.path)
            prepare()
            start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}