package leopardcat.studio.openaitts.data.dto

data class GPTAudioRequest(
    val model: String, //TTS 모델 {tts-1, tts-1-hd} [필수]
    val input: String, //오디오 생성 텍스트 [필수]
    val voice: String, //목소리 {alloy, echo, fable, onyx, nova, shimmer} [필수]
    val response_format: String, //반환 타입 {mp3, opus, aac, flac}
    val speed: Float //재생 속도
)