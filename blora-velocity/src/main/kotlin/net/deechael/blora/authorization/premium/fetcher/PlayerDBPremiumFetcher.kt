package net.deechael.blora.authorization.premium.fetcher

import com.google.gson.JsonParser
import net.deechael.blora.authorization.premium.PremiumPlayer
import net.deechael.blora.authorization.premium.fetcher.PremiumFetcher.FetchResult
import java.io.InputStreamReader
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

class PlayerDBPremiumFetcher : AbstractPremiumFetcher() {

    @OptIn(ExperimentalUuidApi::class)
    override fun fetchPlayer(name: String): FetchResult {
        val response = this.request("https://playerdb.co/api/player/minecraft/${name}")
        return when (response.code) {
            200 -> {
                val body = response.body ?: return FetchResult.ServerError
                val data = JsonParser.parseReader(InputStreamReader(body.byteStream())).asJsonObject


                var id = data["data"].asJsonObject["player"].asJsonObject["id"].asString
                var username = data["data"].asJsonObject["player"].asJsonObject["username"].asString

                FetchResult.Exists(
                    PremiumPlayer(
                        UUID.fromString(id),
                        username
                    )
                )
            }

            400 -> FetchResult.NotExists
            else -> FetchResult.ServerError
        }
    }

}