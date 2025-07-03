package net.deechael.blora.authorization.premium.fetcher

import com.google.gson.JsonParser
import net.deechael.blora.authorization.premium.PremiumPlayer
import net.deechael.blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import net.deechael.blora.extension.fromUndashedString
import java.io.InputStreamReader
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MinetoolsPremiumFetcher : AbstractPremiumFetcher() {

    @OptIn(ExperimentalUuidApi::class)
    override fun fetchPlayer(name: String): FetchResult {
        val response = this.request("https://api.minetools.eu/uuid/${name}")
        return when (response.code) {
            200 -> {
                val body = response.body ?: return FetchResult.ServerError
                val data = JsonParser.parseReader(InputStreamReader(body.byteStream())).asJsonObject

                var rawId = data["id"]
                if (rawId == null || rawId.isJsonNull) {
                    var error = data.get("error")
                    if (error == null) {
                        return FetchResult.NotExists
                    }
                    var errorMessage = error.asString
                    return if (errorMessage.equals("Invalid UUID or nickname.")) {
                        FetchResult.InvalidInput
                    } else {
                        FetchResult.ServerError
                    }
                }

                return if (data["demo"] != null) {
                    FetchResult.NotExists
                } else {
                    FetchResult.Exists(
                        PremiumPlayer(
                            Uuid.fromUndashedString(rawId.asString),
                            data["name"].asString.lowercase()
                        )
                    )
                }
            }

            400 -> FetchResult.NotExists
            else -> FetchResult.ServerError
        }
    }

}