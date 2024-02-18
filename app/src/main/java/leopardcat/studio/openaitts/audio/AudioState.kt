package leopardcat.studio.openaitts.audio

data class AudioState (
    val state: SpeakingState = SpeakingState.NONE
)

enum class SpeakingState {
    AI_SPEAKING,
    USER_SPEAKING,
    AI_LOADING,
    NONE
}