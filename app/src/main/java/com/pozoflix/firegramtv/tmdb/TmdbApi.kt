package com.pozoflix.firegramtv.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class TmdbPage<T>(val page: Int, val results: List<T>)
data class MovieDto(val id: Long, val title: String, val release_date: String?, val poster_path: String?)
data class TvDto(val id: Long, val name: String, val first_air_date: String?, val poster_path: String?)
data class MovieDetailDto(val id: Long, val title: String, val overview: String?, val release_date: String?, val poster_path: String?, val belongs_to_collection: BelongsToCollection?)
data class BelongsToCollection(val id: Long, val name: String)
data class TvDetailDto(val id: Long, val name: String, val overview: String?, val first_air_date: String?, val poster_path: String?, val number_of_seasons: Int)
data class SeasonDto(val id: Long, val name: String, val season_number: Int, val episodes: List<EpisodeDto>)
data class EpisodeDto(val id: Long, val episode_number: Int, val season_number: Int, val name: String, val still_path: String?)
data class CollectionDto(val id: Long, val name: String, val parts: List<MovieDto>)
data class KeywordDto(val id: Long, val name: String)

interface TmdbApi {
    @GET("trending/movie/day") suspend fun trendingMovies(@Query("api_key") key: String, @Query("language") lang: String = "es-ES"): TmdbPage<MovieDto>
    @GET("trending/tv/day") suspend fun trendingTv(@Query("api_key") key: String, @Query("language") lang: String = "es-ES"): TmdbPage<TvDto>
    @GET("movie/popular") suspend fun popularMovies(@Query("api_key") key: String, @Query("language") lang: String = "es-ES"): TmdbPage<MovieDto>
    @GET("tv/popular") suspend fun popularTv(@Query("api_key") key: String, @Query("language") lang: String = "es-ES"): TmdbPage<TvDto>

    @GET("movie/{id}") suspend fun movie(@Path("id") id: Long, @Query("api_key") key: String, @Query("language") lang: String = "es-ES"): MovieDetailDto
    @GET("tv/{id}") suspend fun tv(@Path("id") id: Long, @Query("api_key") key: String, @Query("language") lang: String = "es-ES"): TvDetailDto
    @GET("tv/{id}/season/{s}") suspend fun season(@Path("id") id: Long, @Path("s") s: Int, @Query("api_key") key: String, @Query("language") lang: String = "es-ES"): SeasonDto

    @GET("discover/movie") suspend fun discoverMovies(@Query("api_key") key: String, @Query("language") lang: String = "es-ES", @Query("with_genres") genres: String? = null, @Query("sort_by") sort: String = "popularity.desc"): TmdbPage<MovieDto>
    @GET("search/keyword") suspend fun searchKeyword(@Query("api_key") key: String, @Query("query") q: String): TmdbPage<KeywordDto>
    @GET("collection/{id}") suspend fun collection(@Path("id") id: Long, @Query("api_key") key: String, @Query("language") lang: String = "es-ES"): CollectionDto
}
