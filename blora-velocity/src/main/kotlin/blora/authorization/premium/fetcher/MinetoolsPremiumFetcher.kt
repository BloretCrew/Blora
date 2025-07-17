package blora.authorization.premium.fetcher

import blora.BloraPlugin
import blora.authorization.premium.PremiumPlayer
import blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import blora.extension.fromUndashedString
import com.google.gson.JsonParser
import okhttp3.internal.closeQuietly
import java.io.InputStreamReader
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MinetoolsPremiumFetcher : AbstractPremiumFetcher() {

    @OptIn(ExperimentalUuidApi::class)
    override fun fetchPlayer(name: String): FetchResult {
        val response = this.request("https://api.minetools.eu/uuid/${name}")
        if (response == null)
            return FetchResult.ServerError
        return when (response.code) {
            200 -> {
                try {
                    val body = response.body ?: return FetchResult.ServerError
                    val data = JsonParser.parseReader(InputStreamReader(body.byteStream())).asJsonObject

                    response.close()

                    val rawId = data["id"]
                    if (rawId == null || rawId.isJsonNull) {
                        val error = data.get("error")
                        if (error == null) {
                            return FetchResult.NotExists
                        }
                        val errorMessage = error.asString
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
                } catch (e: Exception) {
                    BloraPlugin.log.info("[LOGIN SYSTEM/Premium Data Fetcher/Minetools] Failed to fetch json")
                    response.closeQuietly()
                    return FetchResult.ServerError
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