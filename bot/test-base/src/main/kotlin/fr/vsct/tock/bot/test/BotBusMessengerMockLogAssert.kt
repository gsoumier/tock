package fr.vsct.tock.bot.test


import fr.vsct.tock.bot.connector.messenger.model.send.*
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions


fun BotBusMockLogAssert.isMessengerAttachmentMessage(): MessengerAttachmentMessageAssert {
        isNotNull

        if (actual().messenger() !is AttachmentMessage) {
            openFailWithMessage("Expected answer to be a Messenger AttachmentMessage but was <%s>", actual())
        }

        return MessengerAttachmentMessageAssert(actual().messenger() as AttachmentMessage)
    }


class MessengerAttachmentMessageAssert(actual: AttachmentMessage) :
    AbstractAssert<MessengerAttachmentMessageAssert, AttachmentMessage>(
        actual,
        MessengerAttachmentMessageAssert::class.java
    ) {



    fun withButtonAttachment(text: String, buttonTitle: String): MessengerAttachmentMessageAssert =
        withButtonAttachment(text, listOf(buttonTitle))

    fun withButtonAttachment(text: String, buttonTitles: List<String>): MessengerAttachmentMessageAssert {
        isNotNull

        if (actual.attachment.payload !is ButtonPayload) {
            failWithMessage(
                "Expected answer to have a ButtonPayload but payload was <%s>",
                actual.attachment.payload.javaClass.kotlin.qualifiedName
            )
        }
        val buttonPayload = actual.attachment.payload as ButtonPayload
        if (buttonPayload.text != text) {
            failWithMessage("Expected ButtonPayload text to be <%s> but was <%s>", text, buttonPayload.text)
        }

        val actualButtonTitles = buttonPayload.buttons
            .map { button ->
                when(button){
                    is PostbackButton -> (button as PostbackButton).title
                    is UrlButton -> (button as UrlButton).title
                    else -> null
                }
            }
        Assertions.assertThat(actualButtonTitles).containsExactlyElementsOf(buttonTitles )

        return this
    }

    fun withTextQuickReplies(quickReplies: List<String>): MessengerAttachmentMessageAssert {
        isNotNull

        val actualTextQuickReplies = actual.quickReplies.orEmpty().filterIsInstance<TextQuickReply>().map { it.title }
        Assertions.assertThat(actualTextQuickReplies).containsExactlyElementsOf(quickReplies)

        return this
    }

}
