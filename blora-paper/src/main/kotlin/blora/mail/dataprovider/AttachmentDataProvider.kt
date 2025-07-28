package blora.mail.dataprovider

import blora.mail.Attachment
import blora.menu.v2.page.PageableDataProvider
import org.bukkit.inventory.ItemStack

class AttachmentDataProvider(
    val attachment: Attachment
) : PageableDataProvider<Pair<ItemStack, UInt>> {

    override val size: Int
        get() = this.attachment.items.size

    override fun get(index: Int): Pair<ItemStack, UInt> {
        return this.attachment.items[index]
    }

    override fun refresh() {
        // do nothing
    }

}