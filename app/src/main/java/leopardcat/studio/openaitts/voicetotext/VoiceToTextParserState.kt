package leopardcat.studio.openaitts.voicetotext

data class VoiceToTextParserState (
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)