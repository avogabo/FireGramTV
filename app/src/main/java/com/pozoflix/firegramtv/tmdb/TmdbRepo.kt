package com.pozoflix.firegramtv.tmdb

import android.content.Context
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TmdbRepo(ctx: Context) {
    private val api: TmdbApi

    init {
        val moshi = Moshi.Builder().build()
        api = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create(TmdbApi::class.java)
    }

    suspend fun trendingMovies(key: String) = api.trendingMovies(key).results
    suspend fun trendingTv(key: String) = api.trendingTv(key).results
    suspend fun popularMovies(key: String) = api.popularMovies(key).results
    suspend fun popularTv(key: String) = api.popularTv(key).results
    suspend fun movie(id: Long, key: String) = api.movie(id, key)
    suspend fun tv(id: Long, key: String) = api.tv(id, key)
    suspend fun season(id: Long, s: Int, key: String) = api.season(id, s, key)
    suspend fun halloween(key: String) = api.discoverMovies(key, genres = "27").results
    suspend fun christmas(key: String): List<MovieDto> {
        return try {
            val results = api.searchKeyword(key, "christmas").results
            val kw = results.firstOrNull()?.id
            if (kw != null) api.discoverMovies(key).results.filter { it.title.contains("navidad", true) || it.title.contains("christmas", true) }
            else api.discoverMovies(key).results.take(20)
        } catch (_: Exception) { api.discoverMovies(key).results.take(20) }
    }
    suspend fun summerBlockbusters(key: String) = api.discoverMovies(key, genres = "28").results
    suspend fun movieCollection(id: Long, key: String) = api.collection(id, key)
}
