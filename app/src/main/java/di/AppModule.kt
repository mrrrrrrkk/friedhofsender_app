package de.friedhofsender.app.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.friedhofsender.app.BuildConfig
import de.friedhofsender.app.audio.MusicPlayer
import de.friedhofsender.app.audio.TtsController
import de.friedhofsender.app.data.GroqApiService
import de.friedhofsender.app.data.GroqRepository
import de.friedhofsender.app.data.WebRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // MusicPlayer benötigt jetzt einen ApplicationContext (für ExoPlayer)
    @Provides
    @Singleton
    fun provideMusicPlayer(
        @ApplicationContext context: Context
    ): MusicPlayer = MusicPlayer(context)

    // TTS benötigt weiterhin Context
    @Provides
    @Singleton
    fun provideTtsController(
        @ApplicationContext context: Context
    ): TtsController = TtsController(context)

    @Provides
    @Singleton
    fun provideWebRepository(): WebRepository = WebRepository()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val logging = HttpLoggingInterceptor { msg ->
            Log.d("HTTP", msg)
        }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val authInterceptor = Interceptor { chain ->
            val original: Request = chain.request()

            val key = BuildConfig.GROQ_API_KEY
            Log.d("GROQ", "KEY IN INTERCEPTOR='$key'")

            val request = original.newBuilder()
                .header("Authorization", "Bearer $key")
                .build()

            Log.d("GROQ", "REQUEST HEADERS: ${request.headers}")

            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGroqApiService(retrofit: Retrofit): GroqApiService =
        retrofit.create(GroqApiService::class.java)

    @Provides
    @Singleton
    fun provideGroqRepository(api: GroqApiService): GroqRepository =
        GroqRepository(api)
}
