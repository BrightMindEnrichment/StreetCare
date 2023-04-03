package org.brightmindenrichment.street_care.YouTube

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeAPI {

    //https://www.googleapis.com/youtube/v3/playlistItems?key=AIzaSyAV5713sUQ-j8KxDjuPGtyVq1aQY1iJkuY&part=id,contentDetails,snippet&playlistId=PLh7GZtyt8qiLKwO_WoE0Vmcu6UMV1AtV9
    // key = AIzaSyAV5713sUQ-j8KxDjuPGtyVq1aQY1iJkuY
    // part = id, contentDetails, snippet
    // playlistId=PLh7GZtyt8qiLKwO_WoE0Vmcu6UMV1AtV9

    @GET("playlistItems")
    suspend fun playlistItems(@Query("key") key: String, @Query("part") part: String, @Query("playlistId") playlistId: String): Playlist
}