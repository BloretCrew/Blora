package net.deechael.blora.authorization.premium.fetcher

import com.google.gson.JsonParser
import net.deechael.blora.authorization.premium.PremiumPlayer
import net.deechael.blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import net.deechael.blora.extension.fromUndashedString
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

            204, 404 -> FetchResult.NotExists
            else -> FetchResult.RateLimit
        }
    }

}