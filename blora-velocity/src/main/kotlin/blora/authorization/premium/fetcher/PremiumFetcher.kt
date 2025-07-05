package blora.authorization.premium.fetcher

import blora.authorization.premium.PremiumPlayer

interface PremiumFetcher {

    fun fetchPlayer(name: String): FetchResult

    sealed interface FetchResult {
        object ServerError : FetchResult
        object InvalidInput : FetchResult
        object RateLimit :
            FetchResult // only appears when using mojang as mojang will always be the latest one be triggered

        object NotExists : FetchResult
        class Exists(val player: PremiumPlayer) : FetchResult
    }

}