package leopardcat.studio.openaitts.data.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import leopardcat.studio.openaitts.data.source.GPTAudioService
import leopardcat.studio.openaitts.data.source.GPTChatService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        private const val AUDIO_URL = "https://api.openai.com/v1/audio/"
        private const val CHAT_URL = "https://api.openai.com/v1/chat/"
    }

    @Provides
    @Named("ApiKey")
    @Singleton
    fun apiKey(): String {
        //TODO API KEY 입력
        return "Bearer sk-~~~~~~~~~~~~~~~~~~~~~"
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(httpLoggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setLenient().create())
    }

    @Provides
    @Singleton
    @Named("Audio")
    fun provideRetrofitAudio(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AUDIO_URL)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()

    @Provides
    @Singleton
    @Named("Chat")
    fun provideRetrofitChat(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(CHAT_URL)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()

    //openAI TTS API
    @Provides
    @Singleton
    fun provideGPTAudioService(
        @Named("Audio")
        retrofit: Retrofit
    ): GPTAudioService {
        return retrofit.create(GPTAudioService::class.java)
    }

    @Provides
    @Singleton
    fun provideGPTChatService(
        @Named("Chat")
        retrofit: Retrofit
    ): GPTChatService {
        return retrofit.create(GPTChatService::class.java)
    }
}