package blora.dialog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtString

@Serializable
enum class AfterAction {

    @SerialName("close")
    CLOSE,

    @SerialName("none")
    NONE,

    @SerialName("wait_for_response")
    WAIT_FOR_RESPONSE;

    fun toNBT(): NbtString {
        return NbtString(this.name.lowercase())
    }

}