package blora.nms

import net.minecraft.commands.CommandBuildContext
import net.minecraft.core.HolderLookup

private var commandBuildContextInternal: CommandBuildContext? = null

val commandBuildContext: CommandBuildContext
    get() {
        if (commandBuildContextInternal == null) {
            val server = nmsServer()

            commandBuildContextInternal = CommandBuildContext.simple(
                server.reloadableRegistries().lookup() as HolderLookup.Provider,
                server.worldData.enabledFeatures()
            )
        }
        return commandBuildContextInternal!!
    }