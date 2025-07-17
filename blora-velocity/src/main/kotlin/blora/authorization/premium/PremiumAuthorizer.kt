package blora.authorization.premium

import blora.BloraPlugin
import blora.authorization.premium.fetcher.MinetoolsPremiumFetcher
import blora.authorization.premium.fetcher.MojangPremiumFetcher
import blora.authorization.premium.fetcher.PlayerDBPremiumFetcher
import blora.authorization.premium.fetcher.PremiumFetcher

object PremiumAuthorizer {

    private val fetchers: MutableList<PremiumFetcher> = mutableListOf()
    private val fallback: PremiumFetcher = MojangPremiumFetcher()

    init {
        this.addFetcher(MinetoolsPremiumFetcher())
        this.addFetcher(PlayerDBPremiumFetcher())
    }

    fun addFetcher(fetcher: PremiumFetcher) {
        if (this.fetchers.contains(fetcher)) {
            return
        }
        this.fetchers.add(fetcher)
    }

    // only three results: Exists, NotExists, ServerError
    // Exists and NotExists are easily understand
    // Server Error for reach the callback fetcher, still cannot fetch valid data (even not exists should have a valid data)
    fun fetchUserByName(username: String): PremiumFetcher.FetchResult {
        for (fetcher in fetchers.toMutableList()
            .apply { this.add(this@PremiumAuthorizer.fallback) /* add fallback fetcher at the last fetcher*/ }) {
            for (i in 0 until 3) { // retry 3 times
                val result = fetcher.fetchPlayer(username)
                BloraPlugin.log.info("[LOGIN SYSTEM/Premium Data Fetcher/${fetcher.javaClass.name}] Retry times: ${i + 1}")
                return when (result) {
                    is PremiumFetcher.FetchResult.Exists -> result
                    is PremiumFetcher.FetchResult.RateLimit -> continue
                    is PremiumFetcher.FetchResult.NotExists -> PremiumFetcher.FetchResult.NotExists
                    is PremiumFetcher.FetchResult.InvalidInput -> PremiumFetcher.FetchResult.NotExists
                    is PremiumFetcher.FetchResult.ServerError -> continue
                }
            }
        }
        return PremiumFetcher.FetchResult.ServerError
    }

}