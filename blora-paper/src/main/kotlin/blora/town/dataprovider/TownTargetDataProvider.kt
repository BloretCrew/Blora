@file:OptIn(ExperimentalUuidApi::class)

package blora.town.dataprovider

import blora.database.DB
import blora.database.town.dao.TownDao
import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider
import blora.town.TownPermissionContainer
import blora.town.TownTarget
import org.jetbrains.exposed.dao.EntityChange
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.toEntity
import kotlin.uuid.ExperimentalUuidApi

class TownTargetDataProvider(
    val town: TownDao
) : PageableDataProvider<TownPermissionContainer> {

    private var filter = mutableListOf<TownTarget>()
    private var loadedData = listOf<TownPermissionContainer>()

    private var hookedMenu: Menu? = null

    private val hook: (EntityChange) -> Unit = { change ->
        if (change.entityClass == TownDao) {
            val entity = change.toEntity(TownDao)
            if (change.changeType == EntityChangeType.Removed && entity != null) {
                if (this.town.townId == entity.townId) {
                    this.hookedMenu?.destroy()
                }
            }
            this.hookedMenu?.rerender()
        }
    }

    override val size: Int
        get() = this.loadedData.size

    override fun get(index: Int): TownPermissionContainer {
        return this.loadedData[index]
    }

    override fun refresh() {
        DB.trans {
            this@TownTargetDataProvider.town.refresh()
        }
        this.loadedData = this.town.permissionContainers
            .filter { permissionContainer ->
                !this.filter.any { target ->
                    if (target is TownTarget.SpecificPlayer && permissionContainer.target is TownTarget.SpecificPlayer) {
                        target.uuid == permissionContainer.target.uuid
                    } else if (target is TownTarget.SpecificRole && permissionContainer.target is TownTarget.SpecificRole) {
                        target.roleId == permissionContainer.target.roleId
                    } else
                        false
                }
            }
            .toList()
    }

    override fun hook(menu: Menu) {
        this.hookedMenu = menu
        EntityHook.subscribe(this.hook)
    }

    override fun unhook() {
        this.hookedMenu = null
        EntityHook.unsubscribe(this.hook)
    }

    internal fun addFilter(filter: TownTarget) {
        this.filter.add(filter)
    }

}