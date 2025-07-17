package blora.command

class CommandMetaBuilderWrapper : blora.internal.api.command.CommandMetaBuilder {

    private var namespace: String = "bloret"
    private var description: String = "Bloret command registered by QuickLib"
    private val aliases: MutableList<String> = mutableListOf()

    override fun namespace(namespace: String): blora.internal.api.command.CommandMetaBuilder {
        this.namespace = namespace
        return this
    }

    override fun description(description: String): blora.internal.api.command.CommandMetaBuilder {
        this.description = description
        return this
    }

    override fun alias(vararg alias: String): blora.internal.api.command.CommandMetaBuilder {
        this.aliases.addAll(alias)
        return this
    }

    override fun build(): blora.internal.api.command.CommandMeta {
        return CommandMetaWrapper(
            this.namespace,
            this.description,
            this.aliases.toList()
        )
    }

}