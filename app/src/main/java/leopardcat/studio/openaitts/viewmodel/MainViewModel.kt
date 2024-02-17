package leopardcat.studio.openaitts.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leopardcat.studio.openaitts.data.dto.GPTAudioRequest
import leopardcat.studio.openaitts.data.repository.GPTAudioRepository
import leopardcat.studio.openaitts.data.repository.GPTChatRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Named

@HiltViewModel
class MainViewModel @javax.inject.Inject constructor(
    private val gptAudioRepository: GPTAudioRepository, //Audio
    private val gptChatRepository: GPTChatRepository, //Chat
    @Named("ApiKey")
    private val apiKey: String, //apiKey
) : ViewModel() {

    //미디어 플레이어
    private var mediaPlayer: MediaPlayer? = null

    //오디오 다운로드 및 재생
    fun makeAudio(context: Context, text: String) {

        //미디어 볼륨 검사
        if(isMediaVolumeZero(context)){
            Toast.makeText(context, "미디어 볼륨을 높여 주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        //채팅, audio API 시작
        viewModelScope.launch(Dispatchers.IO) {
            delay(1500)
            try {
                val chatResponse = gptChatRepository.getGPTChat(
                    apiKey,
                    "application/json",
                    setApiInfo(text)
                )
                chatResponse.choices[0].message.content.let {
                    val destinationFile = File(context.getExternalFilesDir(null), "speech.mp3")
                    downloadAudioFile(destinationFile, chatResponse.choices[0].message.content)
                }
            } catch (e: Exception) {
                Log.e("CHAT API ERROR", "exception : $e")
            }
        }
    }

    private fun setApiInfo(message: String): RequestBody {

        val messageList = mutableListOf<JSONObject>()

        //AI 컨셉
        val concept = "We're doing a role play right now.\n" +
                "assistent is the role of Winnie the Pooh.\n" +
                "User is the role of an elementary school student.\n" +
                "Assistent must be said in English that is easy enough for elementary school students to understand.\n" +
                "Assistent must always be answered in English.\n" +
                "Assistent should be answered briefly, less than 50 characters if possible, and can be answered up to 100 characters.\n" +
                "User and persistent are inseparable friends. Always sharing interesting stories."
        val aiConcept = JSONObject()
        aiConcept.put("role", "system")
        aiConcept.put("content", concept)
        messageList.add(aiConcept)

        //user 메시지
        val userMsg = JSONObject()
        userMsg.put("role", "user")
        userMsg.put("content", message)
        messageList.add(userMsg)

        //Body 생성
        val jsonArray = JSONArray(messageList)
        val requestBody = JSONObject()
        requestBody.put("model", "gpt-3.5-turbo")
        requestBody.put("messages", jsonArray)

        return requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    private suspend fun downloadAudioFile(destination: File, text: String) {
        try {
            val audioResponse = gptAudioRepository.getGPTAudio(
                apiKey,
                "application/json",
                createGPTAudioRequestBody(
                    GPTAudioRequest(
                        "tts-1",
                        text,
                        "alloy",
                        "aac",
                        0.6f
                    )
                )
            )

            audioResponse.let {
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
        } catch (e: Exception) {
            Log.e("AUDIO API ERROR", "exception : $e")
        }
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

    //미디어 볼륨 0이면 return
    private fun isMediaVolumeZero(context: Context): Boolean {
        return getMediaVolume(context) == 0
    }

    //현재 미디어 볼륨값 가져오기
    private fun getMediaVolume(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}