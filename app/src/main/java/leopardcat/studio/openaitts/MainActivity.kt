package leopardcat.studio.openaitts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import dagger.hilt.android.AndroidEntryPoint
import leopardcat.studio.openaitts.audio.SpeakingState
import leopardcat.studio.openaitts.audio.VoiceToTextParser
import leopardcat.studio.openaitts.ui.theme.OpenAITTSTheme
import leopardcat.studio.openaitts.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var voiceToTextParser: VoiceToTextParser

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenAITTSTheme {

                val mainViewModel = hiltViewModel<MainViewModel>()
                voiceToTextParser = VoiceToTextParser(application, mainViewModel)

                var canRecord by remember{
                    mutableStateOf(false)
                }

                val recordAudioLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        canRecord = isGranted
                    }
                )

                //lottie
                val loadingLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.api_loading))
                val aiSpeakingLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ai_speaking))
                val userSpeakingLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.user_speaking))

                LaunchedEffect(key1 = recordAudioLauncher) {
                    recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }

                val state by mainViewModel.state.collectAsState()
                val audioState by mainViewModel.audioState.collectAsState()

                //Lottie animation
                val animationComposition = when (audioState.state) {
                    SpeakingState.USER_SPEAKING -> userSpeakingLottie
                    SpeakingState.AI_SPEAKING -> aiSpeakingLottie
                    SpeakingState.AI_LOADING -> loadingLottie
                    SpeakingState.NONE -> null
                }

                LaunchedEffect(state.spokenText) {
                    if (state.spokenText.isNotEmpty()) {
                        Log.d("hasung", "send message : ${state.spokenText}")
                        mainViewModel.makeAudio(this@MainActivity, state.spokenText) //오디오 다운로드 및 재생
                    }
                }

                LaunchedEffect(audioState.state) {
                    Log.d("hasung", "speaking  = ${audioState.state}")
                }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                if(audioState.state == SpeakingState.USER_SPEAKING) {
                                    voiceToTextParser.stopListening()
                                } else if(audioState.state == SpeakingState.NONE) {
                                    if(mainViewModel.isMediaVolumeZero(this@MainActivity)){ //미디어 볼륨 검사
                                        Toast.makeText(this@MainActivity, "미디어 볼륨을 높여 주세요!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        voiceToTextParser.startListening("ko")
                                    }
                                }
                            }
                        ) {
                            AnimatedContent(targetState = audioState.state, label = "") { speaking ->
                                if(speaking == SpeakingState.USER_SPEAKING) {
                                    Icon(imageVector = Icons.Rounded.Stop, contentDescription = null)
                                } else {
                                    Icon(imageVector = Icons.Rounded.Mic, contentDescription = null)
                                }
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(targetState = audioState.state, label = "") { speaking ->
                            if(speaking == SpeakingState.USER_SPEAKING) {
                                Text(text = "Speaking...")
                            } else {
                                Text(text = state.spokenText.ifEmpty { "Click on mic to record audio" })
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        animationComposition?.let { composition ->
                            LottieAnimation(
                                composition = composition,
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(150.dp),
                                iterations = Int.MAX_VALUE
                            )
                        }
                    }
                }
            }
        }
    }
}