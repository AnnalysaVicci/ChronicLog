package com.anna.chroniclog.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FdaApi {
    // search?search=openfda.brand_name:Lami*&limit=10
    @GET("drug/label.json")
    suspend fun searchDrugs(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20
    ): FdaResponse

    // Wrapper classes to match OpenFDA's JSON structure
    class FdaResponse(val results: List<FdaResult>)
    class FdaResult(val openfda: FdaDrug?)

    companion object {
        private var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("api.fda.gov")
            .build()

        fun create(): FdaApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()

            return Retrofit.Builder()
                .baseUrl(httpurl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FdaApi::class.java)
        }
    }
}