package blora.command

class SuggestionsWrapper(
    override val async: Boolean,
    override val builder: blora.api.command.SuggestionContext.() -> Unit
) : blora.api.command.Suggestions