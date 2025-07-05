package blora.authorization.premium.fetcher

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

abstract class AbstractPremiumFetcher : PremiumFetcher {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5.seconds.toJavaDuration())
        .readTimeout(5.seconds.toJavaDuration())
        .build()

    val gson: Gson = Gson()

    protected fun request(url: String): Response {
        return httpClient.newCall(
            Request.Builder()
                .url(url)
                .build()
        ).execute()
    }


}