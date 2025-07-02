package blora.command

class CommandMetaBuilderWrapper : blora.api.command.CommandMetaBuilder {

    private var namespace: String = "bloret"
    private var description: String = "Bloret command registered by QuickLib"
    private val aliases: MutableList<String> = mutableListOf()

    override fun namespace(namespace: String): blora.api.command.CommandMetaBuilder {
        this.namespace = namespace
        return this
    }

    override fun description(description: String): blora.api.command.CommandMetaBuilder {
        this.description = description
        return this
    }

    override fun alias(vararg alias: String): blora.api.command.CommandMetaBuilder {
        this.aliases.addAll(alias)
        return this
    }

    override fun build(): blora.api.command.CommandMeta {
        return CommandMetaWrapper(
            this.namespace,
            this.description,
            this.aliases.toList()
        )
    }

}