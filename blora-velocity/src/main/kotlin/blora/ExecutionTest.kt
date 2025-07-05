package blora

import blora.item.ItemStack
import plutoproject.adventurekt.item.Items
import plutoproject.adventurekt.item.items.ore.Diamond

fun test() {
    val execution: ExecutionTask = virtual {
        val player = Server.getPlayer("DeeChael")
        player.addItem(ItemStack(Items.Diamond.id, 24))
    }

    // 如果这个 task 没有被 apply，就将为所有已经被发包的玩家撤销所有的操作
    execution.clean()

    // 仍然只是发包给指定玩家，但服务器上的数据没有变化
    execution.replayForPlayer(Server.getPlayer("DeeChael"))
    // 服务器数据发生实际变化
    execution.applyToServer()
}

object Server {

    fun getPlayer(name: String): Player {
        return Player
    }

}

object Player {

    fun addItem(itemStack: ItemStack) {

    }

}

object ExecutionTask {

    fun clean() {

    }

    fun replayForPlayer(player: Player) {

    }

    fun applyToServer() {

    }

}

fun virtual(buildOnly: Boolean = false, context: () -> Unit): ExecutionTask {
    return ExecutionTask
}