package org.brightmindenrichment.street_care.YouTube

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.brightmindenrichment.street_care.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YouTubeDataAdapter {

    val service: YouTubeAPI

    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    }

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(httpClient.build()).build()
        service = retrofit.create(YouTubeAPI::class.java)
    }

    suspend fun getPlaylist(playListId: String) : Playlist {
        val key = BuildConfig.API_KEY  //"AIzaSyAV5713sUQ-j8KxDjuPGtyVq1aQY1iJkuY"
        val part = "id,contentDetails,snippet"
        val playlistId = playListId
        return service.playlistItems(key, part, playlistId)
    }
}