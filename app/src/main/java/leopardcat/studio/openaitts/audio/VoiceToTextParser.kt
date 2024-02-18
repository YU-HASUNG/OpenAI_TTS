package leopardcat.studio.openaitts.audio

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import leopardcat.studio.openaitts.viewmodel.MainViewModel

class VoiceToTextParser(
    private val app: Application,
    private val mainViewModel: MainViewModel
): RecognitionListener {

    private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(app)

    fun startListening(languageCode: String) {
        mainViewModel.updateState(VttState.NONE, null)

        if(!SpeechRecognizer.isRecognitionAvailable(app)) {
            mainViewModel.updateState(VttState.ERROR, "Recognition is not available")
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, languageCode
            )
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)

        mainViewModel.updateAudioState(SpeakingState.USER_SPEAKING)
    }

    fun stopListening() {
        mainViewModel.updateAudioState(SpeakingState.NONE)
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        mainViewModel.updateState(VttState.ERROR, null)
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(p0: Float) = Unit
    override fun onBufferReceived(p0: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        mainViewModel.updateAudioState(SpeakingState.NONE)
    }

    override fun onError(error: Int) {
        if(error == SpeechRecognizer.ERROR_CLIENT) {
            return
        }
        mainViewModel.updateState(VttState.ERROR, error.toString())
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { result ->
                mainViewModel.updateState(VttState.SPOKEN_TEXT, result)
            }
    }

    override fun onPartialResults(p0: Bundle?) = Unit

    override fun onEvent(p0: Int, p1: Bundle?) = Unit
}

enum class VttState {
    ERROR,
    SPOKEN_TEXT,
    NONE
}