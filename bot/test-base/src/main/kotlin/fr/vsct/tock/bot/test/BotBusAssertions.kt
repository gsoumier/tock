package fr.vsct.tock.bot.test

import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import org.assertj.core.api.AbstractAssert

class BotBusAssertions {
    companion object {
        fun assertThat(actual: BotBusMockLog) = BotBusMockLogAssert(actual)
    }

}

class BotBusMockLogAssert (actual: BotBusMockLog) :
    AbstractAssert<BotBusMockLogAssert, BotBusMockLog>(actual, BotBusMockLogAssert::class.java) {

    fun actual() = actual

    fun openFailWithMessage(errorMessage: String, vararg attributes: Any) = failWithMessage(errorMessage, attributes)

    fun isSimpleTextMessage(expectedText : String): BotBusMockLogAssert {
        isNotNull

        val actualText = actual.text() ?: (actual.messenger() as TextMessage).text

        if (actualText != expectedText) {
            failWithMessage("Expected answer to be a simple text <%s> but was <%s>", expectedText, actualText)
        }

        return this
    }

}


