package blora.guild.dataprovider

import blora.configuration.GuildVitalityShopConfiguration
import blora.configuration.GuildVitalityShopGoods
import blora.menu.v2.page.PageableDataProvider

class GuildVitalityShopGoodsDataProvider : PageableDataProvider<GuildVitalityShopGoods> {

    private var loadedData = listOf<GuildVitalityShopGoods>()

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): GuildVitalityShopGoods {
        return this.loadedData[index]
    }

    override fun refresh() {
        this.loadedData = GuildVitalityShopConfiguration.loadOrCreate()
            .goods
            .filter { it.price >= 0 }
            .filter { it.enabled }
    }

}