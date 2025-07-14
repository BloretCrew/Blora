package blora.authorization.premium.fetcher

import blora.authorization.premium.PremiumPlayer
import blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import blora.extension.fromUndashedString
import com.google.gson.JsonParser
import java.io.InputStreamReader
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MojangPremiumFetcher : AbstractPremiumFetcher() {

    @OptIn(ExperimentalUuidApi::class)
    override fun fetchPlayer(name: String): FetchResult {
        val response = this.request("https://api.mojang.com/users/profiles/minecraft/${name}")
        return when (response.code) {
            200 -> {
                val body = response.body ?: return FetchResult.RateLimit
                val data = JsonParser.parseReader(InputStreamReader(body.byteStream())).asJsonObject

                response.close()

                return if (data["demo"] != null) {
                    FetchResult.NotExists
                } else {
                    FetchResult.Exists(
                        PremiumPlayer(
                            Uuid.fromUndashedString(data["id"].asString),
                            data["name"].asString.lowercase()
                        )
                    )
                }
            }

            204, 404 -> {
                response.close()
                FetchResult.NotExists
            }

            else -> {
                response.close()
                FetchResult.RateLimit
            }
        }
    }

}