package blora.command

class CommandMetaWrapper(
    override val namespace: String,
    override val description: String,
    override val aliases: List<String>
) : blora.api.command.CommandMeta