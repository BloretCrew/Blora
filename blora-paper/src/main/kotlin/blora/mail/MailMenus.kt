package blora.mail

import blora.database.mail.dao.MailDao
import blora.database.mail.dao.SystemMailDao
import blora.extension.localization
import blora.extension.openDialog
import blora.internal.api.player.BloraPlayer
import blora.mail.trigger.OnlineBeforeTrigger
import blora.mail.trigger.OnlineInRangeTrigger
import blora.mail.trigger.OnlineInRecentDaysTrigger
import blora.menu.*
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import blora.util.castString
import io.papermc.paper.datacomponent.DataComponentTypes
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*
import plutoproject.adventurekt.text.style.italic
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.style.white
import java.time.LocalDate
import net.momirealms.craftengine.core.util.Key as MomiKey

fun systemMailManagementMenu(
    player: Player
): Menu {
    val systemMails = BloraPlugin.database.listSystemMails().toMutableList()
    return Menu(
        systemMails, // use cached system mails
        player,
        6,
        component {
            localization(player) {
                this.mail.menuSystem_mail_managementTitle
            }
        },
        { menu ->
            menu.destroy()
        }
    ) {}.apply { this.stack.replace(systemMailManagementPage(systemMails, player)) }
}

fun mailListMenu(
    player: Player
): Menu {
    val mails = BloraPlugin.database.getMailsByReceiverUuid(player.uniqueId)
        .filter { it.visible }
        .toMutableList()
    return Menu(
        mails, // use cached system mails
        player,
        6,
        component {
            localization(player) {
                this.mail.menuMail_listTitle
            }
        },
        { menu ->
            menu.destroy()
        }
    ) {}.apply { this.stack.replace(mailListPage(mails, player)) }
}

fun systemMailManagementPage(
    systemMails: MutableList<SystemMailDao>,
    player: Player,
    currentPage: Int = 1
): MenuPage<*> =
    menuPage {
        title {
            localization(player) {
                this.mail.menuSystem_mail_managementTitle
            }
        }
        lines(6)

        mapping(
            "#########",
            "#       #",
            "#       #",
            "#       #",
            "#       #",
            "#########"
        )
        '#' eq {
            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
        }

        if (systemMails.size <= (currentPage - 1) * 28 - 1)
            return@menuPage

        if (currentPage > 1) {
            6 to 1 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(player) {
                            this.menuButtonPrevious_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(systemMailManagementPage(systemMails, player, currentPage - 1))
                }
            }
        }

        if (systemMails.size > (currentPage * 28)) {
            6 to 9 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(player) {
                            this.menuButtonNext_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(systemMailManagementPage(systemMails, player, currentPage + 1))
                }
            }
        }

        systemMails.forEachIndexed { index, systemMail ->
            if (index < (currentPage - 1) * 28 || index > currentPage * 28 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 28
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(
                    CraftEngineItems.byId(MomiKey.of("bloret:mail_read"))?.buildItemStack()
                        ?: ItemStack(Material.BOOK)
                )
                hoverText {
                    title {
                        localization(player = player) {
                            "<italic:false><white>" + systemMail.title
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = player,
                            tags = {
                                componentPlaceholder("sender") {
                                    if (systemMail.sender != null) {
                                        localization(player) {
                                            "<italic:false><white>" + systemMail.sender!!
                                        }
                                    } else {
                                        localization(player) {
                                            "<italic:false><white>" + this.mail.mailSenderSystemDefault
                                        }
                                    }
                                }
                            }
                        ) {
                            "<italic:false><white>" + this.mail.mailTooltipSender
                        }
                        newline()
                        localization(
                            player = player,
                            tags = {
                                componentPlaceholder("date") {
                                    text {
                                        systemMail.sendingDate?.castString() ?: ""
                                    } without italic with white.text
                                }
                            }
                        ) {
                            "<italic:false><white>" + this.mail.mailTooltipSendingDate
                        }
                        newline()
                        localization(
                            player = player,
                            tags = {
                                componentPlaceholder("creator") {
                                    text {
                                        BloraPlugin.database.getPlayerDisplayName(systemMail.creator)
                                    } without italic with white.text
                                }
                            }
                        ) {
                            "<italic:false><white>" + this.mail.mailTooltipCreator
                        }
                        newline()
                        localization(
                            player = player,
                            tags = {
                                componentPlaceholder("date") {
                                    text {
                                        systemMail.createdAt.castString()
                                    } without italic with white.text
                                }
                            }
                        ) {
                            "<italic:false><white>" + this.mail.mailTooltipCreatedAt
                        }
                        newline()
                        localization(player = player) {
                            this.mail.mailSystemTooltipTrigger
                        }
                        for (trigger in systemMail.parsedTriggers) {
                            when (trigger) {
                                is OnlineInRangeTrigger -> {
                                    newline()
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder(
                                                "from",
                                                "<italic:false><white>" + trigger.from.castString()
                                            )
                                            parsedPlaceholder("to", "<italic:false><white>" + trigger.to.castString())
                                        }
                                    ) {
                                        "<italic:false><white>" + this.mail.mailSystemTooltipTriggerOnline_in_range
                                    }
                                }

                                is OnlineBeforeTrigger -> {
                                    newline()
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder(
                                                "date",
                                                "<italic:false><white>" + trigger.date.castString()
                                            )
                                        }
                                    ) {
                                        "<italic:false><white>" + this.mail.mailSystemTooltipTriggerOnline_before
                                    }
                                }

                                is OnlineInRecentDaysTrigger -> {
                                    newline()
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder(
                                                "date",
                                                "<italic:false><white>" + trigger.anchorDate.castString()
                                            )
                                            parsedPlaceholder("days", "<italic:false><white>" + trigger.days.toString())
                                        }
                                    ) {
                                        "<italic:false><white>" + if (trigger.days == 0) {
                                            this.mail.mailSystemTooltipTriggerOnline_in_recent_days_0
                                        } else {
                                            this.mail.mailSystemTooltipTriggerOnline_in_recent_days
                                        }
                                    }
                                }
                            }
                        }
                        newline()
                        newline()
                        localization(
                            player = player,
                            tags = {
                                componentPlaceholder("status") {
                                    localization(player) {
                                        if (systemMail.actived) {
                                            this.mail.mailSystemTooltipStatusActivated
                                        } else {
                                            this.mail.mailSystemTooltipStatusInactivated
                                        }
                                    }
                                }
                            }
                        ) {
                            this.mail.mailSystemTooltipStatus
                        }
                        newline()
                        newline()
                        localization(player) {
                            this.mail.mailTooltipManagementLeft_click
                        }
                        newline()
                        localization(player) {
                            this.mail.mailTooltipManagementRight_click
                        }
                    }
                }
                clickEvent { clickContext ->
                    if (clickContext.clickType.isLeftClick) {
                        player.openDialog(
                            systemMailPreview(player, systemMail)
                        )
                    } else if (clickContext.clickType.isRightClick) {
                        BloraPlugin.database.trans {
                            systemMail.actived = !systemMail.actived
                            systemMail.flush()
                        }
                        clickContext.menu.rerender()
                    }
                    /*
                    For management security, only Rhedar has permission to delete system mails.

                    else if (clickContext.clickType.isRightClick) {
                        BloraPlugin.database.trans {
                            systemMail.delete()
                            systemMail.flush()

                            systemMails.remove(systemMail)
                            clickContext.stack.pop()
                            if (systemMails.size <= (currentPage - 1) * 28 - 1) { // this page does no longer exist
                                clickContext.stack.push(systemMailManagementPage(systemMails, player, currentPage - 1))
                            } else {
                                clickContext.stack.push(systemMailManagementPage(systemMails, player, currentPage))
                            }
                        }
                    }
                     */
                }
            }
        }
    }


fun mailListPage(mails: MutableList<MailDao>, player: Player, currentPage: Int = 1): MenuPage<*> = menuPage {
    title {
        localization(player) {
            this.mail.menuMail_listTitle
        }
    }
    lines(6)

    mapping(
        "#########",
        "#       #",
        "#       #",
        "#       #",
        "#       #",
        "#########"
    )
    '#' eq {
        icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
    }

    if (mails.size <= (currentPage - 1) * 28 - 1)
        return@menuPage

    if (currentPage > 1) {
        6 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonPrevious_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(mailListPage(mails, player, currentPage - 1))
            }
        }
    }

    if (mails.size > (currentPage * 28)) {
        6 to 9 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonNext_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(mailListPage(mails, player, currentPage + 1))
            }
        }
    }

    mails.forEachIndexed { index, mail ->
        if (index < (currentPage - 1) * 28 || index > currentPage * 28 - 1) // not current page
            return@forEachIndexed
        val counterIndex = index - (currentPage - 1) * 28
        ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
            icon(
                if (!mail.isRead && !mail.parsedAttachment.hasNoContent()) {
                    // unread and has attachment
                    CraftEngineItems.byId(MomiKey.of("bloret:mail_unread_attachment"))?.buildItemStack()
                        ?: ItemStack(Material.BOOK) // fallback icon
                } else if (!mail.isRead && mail.parsedAttachment.hasNoContent()) {
                    // unread and has no attachment
                    CraftEngineItems.byId(MomiKey.of("bloret:mail_unread"))?.buildItemStack()
                        ?: ItemStack(Material.BOOK) // fallback icon
                } else if (mail.isRead && !mail.parsedAttachment.hasNoContent() && !mail.isClaim) {
                    // read but not claim attachment
                    CraftEngineItems.byId(MomiKey.of("bloret:mail_read_unclaimed"))?.buildItemStack()
                        ?: ItemStack(Material.BOOK) // fallback i
                } else {
                    // read
                    CraftEngineItems.byId(MomiKey.of("bloret:mail_read"))?.buildItemStack()
                        ?: ItemStack(Material.BOOK) // fallback i
                }
            )
            hoverText {
                title {
                    localization(player = player) {
                        "<italic:false><white>" + mail.title
                    }
                }
                description {
                    newline()
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("sender") {
                                val sender = mail.parsedSender
                                when (sender) {
                                    is Sender.Player -> {
                                        text {
                                            BloraPlugin.database.getPlayerDisplayName(sender.uuid)
                                        }
                                    }

                                    is Sender.System -> {
                                        localization(player) {
                                            sender.name.ifEmpty {
                                                this.mail.mailSenderSystemDefault
                                            }
                                        }
                                    }

                                    else -> {
                                        localization(player) {
                                            this.mail.mailSenderUnknown
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        "<italic:false><white>" + this.mail.mailTooltipSender
                    }
                    newline()
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("date") {
                                text {
                                    mail.createdAt.castString()
                                } without italic with white.text
                            }
                        }
                    ) {
                        "<italic:false><white>" + this.mail.mailTooltipCreatedAt
                    }
                    newline()
                    newline()
                    localization(player) {
                        "<italic:false><white>" + this.mail.mailTooltipView
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    BloraPlugin.database.trans {
                        mail.isRead = true
                        mail.flush()
                    }
                    player.openDialog(
                        playerViewMail(player, mail)
                    )
                }
            }
        }
    }
}

fun createSystemMailMenu(player: BloraPlayer): Menu {
    return createSystemMailMenu(player.asBukkit)
}

fun createSystemMailMenu(player: Player): Menu {
    return Menu(CreatingMailContext(), player, 5, component {
        localization(player) {
            this.mail.menuCreate_system_mailTitle
        }
    }, {
        if (!(it.contextObject as CreatingMailContext).modifying) {
            it.destroy()
        } else {
            it.open() // reopen
            it.stack.pop()
            it.contextObject.modifying = false
        }
    }) {
        title {
            localization(player) {
                this.mail.menuCreate_system_mailTitle
            }
        }
        lines(5)
        2 to 2 eq {
            icon(ItemStack(Material.PAINTING))
            hoverText { menu ->
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_title
                    }
                }
                if ((menu.contextObject as CreatingMailContext).title.isNotEmpty()) {
                    description {
                        newline()
                        localization(menu.viewer) {
                            "<italic:false><white>${menu.contextObject.title}"
                        }
                    }
                }
            }
            clickEvent {
                (it.menu.contextObject as CreatingMailContext).modifying = true
                player.openDialog(
                    modifyMailTitleDialog(player, it.menu, it.menu.contextObject as CreatingMailContext)
                )
            }
        }
        2 to 5 eq {
            icon(ItemStack(Material.NAME_TAG))
            hoverText { menu ->
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_system_sender
                    }
                }
                if ((menu.contextObject as CreatingMailContext).sender.isNotEmpty()) {
                    description {
                        newline()
                        localization(menu.viewer) {
                            "<italic:false><white>${menu.contextObject.sender}"
                        }
                    }
                }
            }
            clickEvent {
                (it.menu.contextObject as CreatingMailContext).modifying = true
                player.openDialog(
                    modifyMailSystemSenderDialog(player, it.menu, it.menu.contextObject as CreatingMailContext)
                )
            }
        }
        2 to 8 eq {
            icon(ItemStack(Material.WRITABLE_BOOK))
            hoverText { menu ->
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_contents
                    }
                }
                if ((menu.contextObject as CreatingMailContext).contents.isNotEmpty()) {
                    description {
                        menu.contextObject.contents
                            .split("\n")
                            .map { it.split("<newline>") }
                            .flatten()
                            .forEach { loreLine ->
                                newline()
                                localization(menu.viewer) {
                                    "<italic:false><white>${loreLine}"
                                }
                            }
                    }
                }
            }
            clickEvent {
                (it.menu.contextObject as CreatingMailContext).modifying = true
                player.openDialog(
                    modifyMailContentsDialog(player, it.menu, it.menu.contextObject as CreatingMailContext)
                )
            }
        }
        4 to 2 eq {
            icon(ItemStack(Material.REDSTONE_TORCH))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_trigger
                    }
                }
            }
            clickEvent {
                it.menu.stack.push(modifyTriggersMenuPage(it.menu.contextObject as CreatingMailContext, it.menu.viewer))
            }
        }
        4 to 5 eq {
            icon(ItemStack(Material.CHEST))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_attachment
                    }
                }
            }
            clickEvent {
                it.menu.stack.push(
                    modifyAttachmentMenuPage(
                        it.menu.contextObject as CreatingMailContext,
                        it.menu.viewer
                    )
                )
            }
        }
        4 to 8 eq {
            icon(ItemStack(Material.CLOCK))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_date
                    }
                }
                description {
                    if ((it.contextObject as CreatingMailContext).date != null) {
                        newline()
                        text {
                            it.contextObject.date!!.castString()
                        } without italic with white.text
                        newline()
                        localization(player) {
                            this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                        }
                        newline()
                        localization(player) {
                            this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip2
                        }
                    } else {
                        newline()
                        localization(player) {
                            this.mail.menuCreate_system_mailButtonModify_dateDescriptionNot_set
                        }
                    }
                }
            }
            clickEvent { clickContext ->
                val context = (clickContext.menu.contextObject as CreatingMailContext)
                if (clickContext.clickType.isLeftClick) {
                    context.modifying = true
                    clickContext.menu.viewer.openDialog(
                        modifyDateTimeDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_datetimeModify_receive_dateTitle
                                }
                            },
                            yesCallback = { date ->
                                context.date = date
                                clickContext.menu.rerender()
                                context.modifying = false
                            },
                            noCallback = {
                                clickContext.menu.rerender()
                                context.modifying = false
                            }
                        )
                    )
                } else if (clickContext.clickType.isRightClick) {
                    context.date = null
                    clickContext.menu.rerender()
                }
            }
        }
        5 to 9 eq {
            icon(ItemStack(Material.EMERALD))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonCreate
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    val creatingContext = (clickContext.menu.contextObject as CreatingMailContext)
                    if (creatingContext.title.isEmpty()) {
                        player.send {
                            localization(player) {
                                this.mail.menuCreate_system_mailWarningTitle_cannot_be_empty
                            }
                        }
                        return@clickEvent
                    }
                    clickContext.menu.destroy()
                    if (!player.hasPermission(Permissions.Admin)) {
                        player.send {
                            localization(player) {
                                this.mail.mailErrorNo_permission_to_create_system_mail
                            }
                        }
                        return@clickEvent
                    }
                    player.openDialog(
                        setSystemMailIdentifierDialog(
                            creatingContext,
                            player
                        )
                    )
                }
            }
        }
    }
}

fun modifyTriggersMenuPage(context: CreatingMailContext, player: Player) = menuPage {
    title {
        localization(player) {
            this.mail.menuCreate_system_mailPageModify_triggersTitle
        }
    }
    lines(5)
    1 to 1 eq {
        icon(ItemStack(Material.ARROW))
        hoverText {
            title {
                localization(player) {
                    this.menuButtonBack
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }
    3 to 2 eq {
        icon(ItemStack(Material.MINECART))
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_in_range
                }
            }
            description {
                val trigger = context.triggers.find { it is OnlineInRangeTrigger } as OnlineInRangeTrigger?
                if (trigger != null) {
                    newline()
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("from", trigger.from.castString())
                            parsedPlaceholder("to", trigger.to.castString())
                        }
                    ) {
                        this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_in_rangeDescription
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip2
                    }
                } else {
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_triggersButtonDescriptionNot_set
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                val trigger = context.triggers.find { it is OnlineInRangeTrigger }.let {
                    if (it != null)
                        it
                    else {
                        val newTrigger = OnlineInRangeTrigger(
                            LocalDate.now().minusDays(1),
                            LocalDate.now()
                        )
                        context.triggers.add(newTrigger)
                        newTrigger
                    }
                } as OnlineInRangeTrigger
                clickContext.stack.push(
                    modifyOnlineInRangeTrigger(
                        context,
                        trigger,
                        player
                    )
                )
            } else if (clickContext.clickType.isRightClick) {
                val expectedTrigger = context.triggers.find { it is OnlineInRangeTrigger }
                if (expectedTrigger != null) {
                    context.triggers.remove(expectedTrigger)
                    clickContext.menu.rerender()
                }
            }
        }
    }
    3 to 5 eq {
        icon(ItemStack(Material.ENDER_EYE))
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_before
                }
            }
            description {
                val trigger = context.triggers.find { it is OnlineBeforeTrigger } as OnlineBeforeTrigger?
                if (trigger != null) {
                    newline()
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("date", trigger.date.castString())
                        }
                    ) {
                        this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_beforeDescription
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip2
                    }
                } else {
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_triggersButtonDescriptionNot_set
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                context.modifying = true
                clickContext.menu.viewer.openDialog(
                    modifyDateDialog(
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.mail.dialogModify_dateModify_online_before_triggerTitle
                            }
                        },
                        yesCallback = { date ->
                            val expectedTrigger = context.triggers.find { it is OnlineBeforeTrigger }.let {
                                if (it != null) {
                                    context.triggers.remove(it)
                                    this
                                } else {
                                    OnlineBeforeTrigger(LocalDate.now())
                                }
                            } as OnlineBeforeTrigger

                            expectedTrigger.date = date
                            context.triggers.add(expectedTrigger)
                            clickContext.menu.rerender()
                            context.modifying = false
                        },
                        noCallback = {
                            clickContext.menu.rerender()
                            context.modifying = false
                        }
                    )
                )
            } else if (clickContext.clickType.isRightClick) {
                val expectedTrigger = context.triggers.find { it is OnlineBeforeTrigger }
                if (expectedTrigger != null) {
                    context.triggers.remove(expectedTrigger)
                    clickContext.menu.rerender()
                }
            }
        }
    }
    3 to 8 eq {
        icon(ItemStack(Material.CLOCK))
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_in_days
                }
            }
            description {
                val trigger = context.triggers.find { it is OnlineInRecentDaysTrigger } as OnlineInRecentDaysTrigger?
                if (trigger != null) {
                    newline()
                    if (trigger.days == 0) {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("date", trigger.anchorDate.castString())
                            }
                        ) {
                            this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_in_daysDescription_0days
                        }
                    } else {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("date", trigger.anchorDate.castString())
                                parsedPlaceholder("days", trigger.days.toString())
                            }
                        ) {
                            this.mail.menuCreate_system_mailPageModify_triggersButtonOnline_in_daysDescription
                        }
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip2
                    }
                } else {
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_triggersButtonDescriptionNot_set
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailButtonModify_dateDescriptionTooltip1
                    }
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                val trigger = context.triggers.find { it is OnlineInRecentDaysTrigger }.let {
                    if (it != null)
                        it
                    else {
                        val newTrigger = OnlineInRecentDaysTrigger(
                            LocalDate.now(),
                            1
                        )
                        context.triggers.add(newTrigger)
                        newTrigger
                    }
                } as OnlineInRecentDaysTrigger
                clickContext.stack.push(
                    modifyOnlineInRecentDaysTrigger(
                        context,
                        trigger,
                        player
                    )
                )
            } else if (clickContext.clickType.isRightClick) {
                val expectedTrigger = context.triggers.find { it is OnlineInRecentDaysTrigger }
                if (expectedTrigger != null) {
                    context.triggers.remove(expectedTrigger)
                    clickContext.menu.rerender()
                }
            }
        }
    }
}

fun modifyOnlineInRangeTrigger(context: CreatingMailContext, trigger: OnlineInRangeTrigger, player: Player) = menuPage {
    title {
        localization(player) {
            this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeTitle
        }
    }
    lines(5)
    1 to 1 eq {
        icon(ItemStack(Material.ARROW))
        hoverText {
            title {
                localization(player) {
                    this.menuButtonBack
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }
    3 to 3 eq {
        icon(ItemStack(Material.SHEARS))
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeButtonModify_from
                }
            }
            description {
                newline()
                mini("<italic:false><white>${trigger.from.castString()}")
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                context.modifying = true
                clickContext.menu.viewer.openDialog(
                    modifyOnlineInRangeDialog(
                        trigger,
                        true,
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.mail.dialogModify_dateModify_online_before_triggerTitle
                            }
                        },
                        yesCallback = { date ->
                            trigger.from = date
                            clickContext.menu.rerender()
                            context.modifying = false
                        },
                        noCallback = {
                            clickContext.menu.rerender()
                            context.modifying = false
                        }
                    )
                )
            }
        }
    }
    3 to 7 eq {
        icon(ItemStack(Material.SPYGLASS))
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeButtonModify_to
                }
            }
            description {
                newline()
                mini("<italic:false><white>${trigger.to.castString()}")
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                context.modifying = true
                clickContext.menu.viewer.openDialog(
                    modifyOnlineInRangeDialog(
                        trigger,
                        false,
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.mail.dialogModify_dateModify_online_before_triggerTitle
                            }
                        },
                        yesCallback = { date ->
                            trigger.to = date
                            clickContext.menu.rerender()
                            context.modifying = false
                        },
                        noCallback = {
                            clickContext.menu.rerender()
                            context.modifying = false
                        }
                    )
                )
            }
        }
    }
}

fun modifyOnlineInRecentDaysTrigger(context: CreatingMailContext, trigger: OnlineInRecentDaysTrigger, player: Player) =
    menuPage {
        title {
            localization(player) {
                this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_recent_daysTitle
            }
        }
        lines(5)
        1 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent {
                it.stack.pop()
            }
        }
        3 to 3 eq {
            icon(ItemStack(Material.SHEARS))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_rangeButtonModify_from
                    }
                }
                description {
                    newline()
                    mini("<italic:false><white>${trigger.anchorDate.castString()}")
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    context.modifying = true
                    clickContext.menu.viewer.openDialog(
                        modifyDateDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_dateModify_online_before_triggerTitle
                                }
                            },
                            yesCallback = { date ->
                                trigger.anchorDate = date
                                clickContext.menu.rerender()
                                context.modifying = false
                            },
                            noCallback = {
                                clickContext.menu.rerender()
                                context.modifying = false
                            }
                        )
                    )
                }
            }
        }
        3 to 7 eq {
            icon(ItemStack(Material.SPYGLASS))
            hoverText {
                title {
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_recent_daysButtonModify_days
                    }
                }
                description {
                    newline()
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("days", trigger.days.toString())
                        }
                    ) {
                        this.mail.menuCreate_system_mailPageModify_triggersSub_pageModify_online_in_recent_daysButtonModify_daysDescription
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    context.modifying = true
                    clickContext.menu.viewer.openDialog(
                        modifyDaysDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_daysTitle
                                }
                            },
                            yesCallback = { days ->
                                trigger.days = days
                                clickContext.menu.rerender()
                                context.modifying = false
                            },
                            noCallback = {
                                clickContext.menu.rerender()
                                context.modifying = false
                            }
                        )
                    )
                }
            }
        }
    }

fun modifyAttachmentMenuPage(
    creatingContext: CreatingMailContext,
    player: Player,
    currentPage: Int = 1
): MenuPage<*> = menuPage {
    title {
        localization(player) {
            this.mail.menuCreate_system_mailPageModify_attachmentTitle
        }
    }
    lines(5)

    mapping(
        "#########",
        "#       #",
        "#       #",
        "#       #",
        "#########",
    )

    '#' eq {
        icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
    }

    inventoryClick { item, context ->
        if (context.clickType.isLeftClick) {
            if (creatingContext.attachment.itemsContainsLike(item)) {
                return@inventoryClick true
            }
            creatingContext.attachment.items.add(item to 1u)

            // rerender cannot solve the add item problem, recreate current page to modify
            context.stack.pop()
            context.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage))
        }
        return@inventoryClick true
    }

    1 to 1 eq {
        icon(ItemStack(Material.ARROW))
        hoverText {
            title {
                localization(player) {
                    this.menuButtonBack
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }

    // do not show anything
    if (creatingContext.attachment.items.size <= (currentPage - 1) * 21 - 1)
        return@menuPage

    if (currentPage > 1) {
        5 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonPrevious_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage - 1))
            }
        }
    }

    if (creatingContext.attachment.items.size > (currentPage * 21)) {
        5 to 9 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonNext_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage + 1))
            }
        }
    }

    5 to 4 eq {
        icon(
            CraftEngineItems.byId(MomiKey.of("bloret:coin"))?.buildItemStack()
                ?: ItemStack(Material.SUNFLOWER) // fallback icon
        )
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoinTitle
                }
            }
            description {
                newline()
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder("amount", creatingContext.attachment.coins.toString())
                    }
                ) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                creatingContext.modifying = true
                clickContext.menu.viewer.openDialog(
                    modifyAmountDialog(
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.mail.dialogModify_amountTitle
                            }
                        },
                        yesCallback = { amount ->
                            creatingContext.attachment.coins = amount
                            clickContext.menu.rerender()
                            creatingContext.modifying = false
                        },
                        noCallback = {
                            clickContext.menu.rerender()
                            creatingContext.modifying = false
                        }
                    )
                )
            } else if (clickContext.clickType.isRightClick) {
                creatingContext.attachment.coins = 0u
                clickContext.menu.rerender()
            }
        }
    }

    5 to 6 eq {
        icon(
            CraftEngineItems.byId(MomiKey.of("bloret:blorius"))?.buildItemStack()
                ?: ItemStack(Material.DIAMOND) // fallback icon
        )
        hoverText {
            title {
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentBloriusTitle
                }
            }
            description {
                newline()
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder("amount", creatingContext.attachment.blorius.toString())
                    }
                ) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(player) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                creatingContext.modifying = true
                clickContext.menu.viewer.openDialog(
                    modifyAmountDialog(
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.mail.dialogModify_amountTitle
                            }
                        },
                        yesCallback = { amount ->
                            creatingContext.attachment.blorius = amount
                            clickContext.menu.rerender()
                            creatingContext.modifying = false
                        },
                        noCallback = {
                            clickContext.menu.rerender()
                            creatingContext.modifying = false
                        }
                    )
                )
            } else if (clickContext.clickType.isRightClick) {
                creatingContext.attachment.blorius = 0u
                clickContext.menu.rerender()
            }
        }
    }

    creatingContext.attachment.items.toList().forEachIndexed { index, item ->
        if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
            return@forEachIndexed
        val counterIndex = index - (currentPage - 1) * 21
        ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
            icon(item.first.clone())
            hoverText {
                title {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("amount", item.second.toString())
                        }
                    ) {
                        this.mail.menuCreate_system_mailPageModify_attachmentItemTitle
                    }
                }
                description {
                    newline()
                    localization(player = player) {
                        this.mail.menuCreate_system_mailPageModify_attachmentItemTooltip1
                    }
                    raw { item.first.getData(DataComponentTypes.ITEM_NAME) ?: item.first.displayName() }

                    val lore = item.first.getData(DataComponentTypes.LORE)
                    if (lore != null && lore.lines().isNotEmpty()) {
                        newline()
                        newline()
                        localization(player = player) {
                            this.mail.menuCreate_system_mailPageModify_attachmentItemTooltip2
                        }
                        newline()
                        for (line in lore.lines()) {
                            raw { line }
                        }
                    }
                    newline()
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                    }
                    newline()
                    localization(player) {
                        this.mail.menuCreate_system_mailPageModify_attachmentItemDescription2
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    creatingContext.modifying = true
                    clickContext.menu.viewer.openDialog(
                        modifyAmountDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_amountTitle
                                }
                            },
                            yesCallback = { amount ->
                                creatingContext.attachment.items[
                                    creatingContext.attachment.items.indexOf(item)
                                ] = item.first to amount
                                clickContext.stack.pop()
                                clickContext.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage))
                                creatingContext.modifying = false
                            },
                            noCallback = {
                                clickContext.stack.pop()
                                clickContext.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage))
                                creatingContext.modifying = false
                            }
                        )
                    )
                } else if (clickContext.clickType.isRightClick) {
                    creatingContext.attachment.items.remove(item)
                    clickContext.stack.pop()
                    if (creatingContext.attachment.items.size <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                        clickContext.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage - 1))
                    } else {
                        clickContext.stack.push(modifyAttachmentMenuPage(creatingContext, player, currentPage))
                    }
                }
            }
        }
    }
}