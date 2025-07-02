package net.deechael.blora.authorization.premium

import net.deechael.blora.authorization.premium.fetcher.MinetoolsPremiumFetcher
import net.deechael.blora.authorization.premium.fetcher.MojangPremiumFetcher
import net.deechael.blora.authorization.premium.fetcher.PlayerDBPremiumFetcher
import net.deechael.blora.authorization.premium.fetcher.PremiumFetcher

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
        for (fetcher in fetchers.toMutableList().apply { this.add(this@PremiumAuthorizer.fallback) /* add fallback fetcher at the last fetcher*/ }) {
            val result = fetcher.fetchPlayer(username)
            return when (result) {
                is PremiumFetcher.FetchResult.Exists ->
                    result

                is PremiumFetcher.FetchResult.RateLimit -> PremiumFetcher.FetchResult.ServerError
                is PremiumFetcher.FetchResult.ServerError -> continue
                is PremiumFetcher.FetchResult.NotExists -> PremiumFetcher.FetchResult.NotExists
                is PremiumFetcher.FetchResult.InvalidInput -> PremiumFetcher.FetchResult.NotExists
            }
        }
        return PremiumFetcher.FetchResult.NotExists
    }

}