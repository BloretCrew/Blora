package blora.authorization.premium.fetcher

import blora.BloraPlugin
import blora.authorization.premium.PremiumPlayer
import blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import com.google.gson.JsonParser
import okhttp3.internal.closeQuietly
import java.io.InputStreamReader
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

class PlayerDBPremiumFetcher : AbstractPremiumFetcher() {

    @OptIn(ExperimentalUuidApi::class)
    override fun fetchPlayer(name: String): FetchResult {
        val response = this.request("https://playerdb.co/api/player/minecraft/${name}")
        if (response == null)
            return FetchResult.ServerError
        return when (response.code) {
            200 -> {
                val body = response.body ?: return FetchResult.ServerError
                try {
                    val data = JsonParser.parseReader(InputStreamReader(body.byteStream())).asJsonObject

                    response.close()

                    val id = data["data"].asJsonObject["player"].asJsonObject["id"].asString
                    val username = data["data"].asJsonObject["player"].asJsonObject["username"].asString

                    FetchResult.Exists(
                        PremiumPlayer(
                            UUID.fromString(id),
                            username
                        )
                    )
                } catch (e: Exception) {
                    BloraPlugin.log.info("[LOGIN SYSTEM/Premium Data Fetcher/PlayerDB] Failed to fetch json")
                    response.closeQuietly()
                    FetchResult.ServerError
                }
            }

            400 -> {
                response.close()
                FetchResult.NotExists
            }

            else -> {
                response.close()
                FetchResult.ServerError
            }
        }
    }

}