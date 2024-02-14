package leopardcat.studio.openaitts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dagger.hilt.android.AndroidEntryPoint
import leopardcat.studio.openaitts.ui.theme.OpenAITTSTheme
import leopardcat.studio.openaitts.viewmodel.MainViewModel
import leopardcat.studio.openaitts.voicetotext.VoiceToTextParser

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val voiceToTextParser by lazy {
        VoiceToTextParser(application)
    }

    @SuppressLint("UnusedContentLambdaTargetStateParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenAITTSTheme {

                val mainViewModel = hiltViewModel<MainViewModel>()

                var canRecord by remember{
                    mutableStateOf(false)
                }

                val recordAudioLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        canRecord = isGranted
                    }
                )

                LaunchedEffect(key1 = recordAudioLauncher) {
                    recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }

                val state by voiceToTextParser.state.collectAsState()

                LaunchedEffect(state.spokenText) {
                    if (state.spokenText.isNotEmpty()) {
                        Log.d("hasung", "send message : ${state.spokenText}")
                        mainViewModel.makeAudio(this@MainActivity, state.spokenText)//오디오 다운로드 및 재생
                    }
                }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                if(state.isSpeaking) {
                                    voiceToTextParser.stopListening()
                                } else {
                                    voiceToTextParser.startListening("ko")
                                }
                            }
                        ) {
                            AnimatedContent(targetState = state.isSpeaking, label = "") { isSpeaking ->
                                if(isSpeaking) {
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
                        AnimatedContent(targetState = state.isSpeaking, label = "") { isSpeaking ->
                            if(isSpeaking) {
                                Text(text = "Speaking...")
                            } else {
                                Text(text = state.spokenText.ifEmpty { "Click on mic to record audio" })
                            }
                        }
                    }
                }

//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}