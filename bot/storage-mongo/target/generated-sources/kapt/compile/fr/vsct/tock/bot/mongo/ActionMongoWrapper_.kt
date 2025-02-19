package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.dialog.EventState
import fr.vsct.tock.bot.engine.dialog.EventState_
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerId_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Id: KProperty1<DialogCol.ActionMongoWrapper, Id<Action>?>
    get() = DialogCol.ActionMongoWrapper::id
private val __Date: KProperty1<DialogCol.ActionMongoWrapper, Instant?>
    get() = DialogCol.ActionMongoWrapper::date
private val __State: KProperty1<DialogCol.ActionMongoWrapper, EventState?>
    get() = DialogCol.ActionMongoWrapper::state
private val __BotMetadata: KProperty1<DialogCol.ActionMongoWrapper, ActionMetadata?>
    get() = DialogCol.ActionMongoWrapper::botMetadata
private val __PlayerId: KProperty1<DialogCol.ActionMongoWrapper, PlayerId?>
    get() = DialogCol.ActionMongoWrapper::playerId
private val __RecipientId: KProperty1<DialogCol.ActionMongoWrapper, PlayerId?>
    get() = DialogCol.ActionMongoWrapper::recipientId
private val __ApplicationId: KProperty1<DialogCol.ActionMongoWrapper, String?>
    get() = DialogCol.ActionMongoWrapper::applicationId
internal open class ActionMongoWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogCol.ActionMongoWrapper?>) : KPropertyPath<T,
        DialogCol.ActionMongoWrapper?>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = KPropertyPath(this,__BotMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    companion object {
        val Id: KProperty1<DialogCol.ActionMongoWrapper, Id<Action>?>
            get() = __Id
        val Date: KProperty1<DialogCol.ActionMongoWrapper, Instant?>
            get() = __Date
        val State: EventState_<DialogCol.ActionMongoWrapper>
            get() = EventState_(null,__State)
        val BotMetadata: KProperty1<DialogCol.ActionMongoWrapper, ActionMetadata?>
            get() = __BotMetadata
        val PlayerId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() = PlayerId_(null,__PlayerId)
        val RecipientId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() = PlayerId_(null,__RecipientId)
        val ApplicationId: KProperty1<DialogCol.ActionMongoWrapper, String?>
            get() = __ApplicationId}
}

internal open class ActionMongoWrapper_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogCol.ActionMongoWrapper>?>) : KCollectionPropertyPath<T,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = KPropertyPath(this,__BotMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}

internal open class ActionMongoWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogCol.ActionMongoWrapper>?>) : KMapPropertyPath<T, K,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = KPropertyPath(this,__BotMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}
