package blora.authorization.premium.fetcher

import blora.BloraPlugin
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

    protected fun request(url: String): Response? {
        for (i in 0 until 3) { // retry 3 times
            BloraPlugin.log.info("[LOGIN SYSTEM/Premium Data Fetcher/${this.javaClass.name}] Internal retry times: ${i + 1}")
            try {
                return httpClient.newCall(
                    Request.Builder()
                        .url(url)
                        .build()
                ).execute()
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }


}