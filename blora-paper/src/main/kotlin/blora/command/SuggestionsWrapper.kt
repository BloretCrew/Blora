package blora.command

class SuggestionsWrapper(
    override val async: Boolean,
    override val builder: blora.internal.api.command.SuggestionContext.() -> Unit
) : blora.internal.api.command.Suggestions