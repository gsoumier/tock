/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.test

import com.github.salomonbrys.kodein.instance
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.dialog.DialogReportDAO
import fr.vsct.tock.bot.connector.rest.client.ConnectorRestClient
import fr.vsct.tock.bot.connector.rest.client.model.ClientConnectorType
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessage
import fr.vsct.tock.bot.connector.rest.client.model.ClientMessageRequest
import fr.vsct.tock.bot.connector.rest.client.model.ClientSentence
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Duration
import java.time.Instant

/**
 *
 */
object TestPlanService {

    private val logger = KotlinLogging.logger {}

    private val testPlanDAO: TestPlanDAO by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()
    private val dialogDAO: DialogReportDAO by injector.instance()
    private val botConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()

    private val applicationIdPathCache: Cache<String, String> =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(1)).build()

    fun getPlanExecutions(plan: TestPlan): List<TestPlanExecution> {
        return testPlanDAO.getPlanExecutions(plan._id)
    }

    fun getTestPlansByNamespaceAndNlpModel(namespace: String, nlpModel: String): List<TestPlan> {
        return testPlanDAO.getPlans().filter { it.namespace == namespace && it.nlpModel == nlpModel }
    }

    fun getTestPlansByNamespace(namespace: String): List<TestPlan> {
        return testPlanDAO.getPlans().filter { it.namespace == namespace }
    }

    fun getTestPlansByApplication(applicationId: String): List<TestPlan> {
        return testPlanDAO.getPlansByApplicationId(applicationId)
    }

    fun removeDialogFromTestPlan(plan: TestPlan, dialogId: Id<Dialog>) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs.filter { it.id != dialogId }))
    }

    fun addDialogToTestPlan(plan: TestPlan, dialogId: Id<Dialog>) {
        saveTestPlan(plan.copy(dialogs = plan.dialogs + TestDialogReport(dialogDAO.getDialog(dialogId)!!)))
    }

    fun removeTestPlan(plan: TestPlan) {
        testPlanDAO.removeTestPlan(plan._id)
    }

    fun saveTestPlan(plan: TestPlan) {
        testPlanDAO.save(plan)
    }

    fun getTestPlan(planId: Id<TestPlan>): TestPlan? {
        return testPlanDAO.getPlan(planId)
    }

    fun saveAndRunTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        testPlanDAO.save(plan)
        return runTestPlan(client, plan)
    }

    fun runTestPlan(client: ConnectorRestClient, plan: TestPlan): TestPlanExecution {
        val start = Instant.now()
        val dialogs: MutableList<DialogExecutionReport> = mutableListOf()
        var nbErrors: Int = 0
        plan.dialogs.forEach {
            runDialog(client, plan, it).run {
                dialogs.add(this)
                if (error) {
                    nbErrors++
                }
            }
        }
        val exec = TestPlanExecution(
            plan._id,
            dialogs,
            nbErrors,
            duration = Duration.between(start, Instant.now())
        )
        testPlanDAO.save(exec)
        return exec
    }


    private fun runDialog(
        client: ConnectorRestClient,
        testPlan: TestPlan,
        dialog: TestDialogReport
    ): DialogExecutionReport {
        val playerId = Dice.newId()
        val botId = Dice.newId()
        return try {
            var botMessages: MutableList<ClientMessage> = mutableListOf()
            //send first action if specified
            if (testPlan.startAction != null) {
                client.talk(
                    getPath(testPlan),
                    testPlan.locale,
                    ClientMessageRequest(
                        playerId,
                        botId,
                        testPlan.startAction!!.toClientMessage(),
                        testPlan.targetConnectorType.toClientConnectorType(),
                        true
                    )
                )
            }

            dialog.actions.forEach {
                if (it.playerId.type == PlayerType.user) {
                    val request = ClientMessageRequest(
                        playerId,
                        botId,
                        it.findFirstMessage().toClientMessage(),
                        testPlan.targetConnectorType.toClientConnectorType(),
                        true
                    )
                    logger.debug { "ask: $request" }
                    val answer = client.talk(getPath(testPlan), testPlan.locale, request)
                    if (answer.isSuccessful) {
                        val body = answer.body()
                        logger.debug { "answer: $body" }
                        botMessages = body?.messages?.toMutableList() ?: mutableListOf()
                    } else {
                        logger.error { answer.errorBody()?.string() }
                        return DialogExecutionReport(
                            dialog.id, true, errorMessage = answer.errorBody()?.toString()
                                    ?: "unknown error"
                        )
                    }
                } else {
                    if (botMessages.isEmpty()) {
                        return DialogExecutionReport(
                            dialog.id,
                            true,
                            it.id,
                            errorMessage = "(no answer but one expected)"
                        )
                    }
                    val botMessage = botMessages.removeAt(0)
                    if (!botMessage.deepEquals(it)) {
                        logger.info { "no same message:\n$botMessage\n${it.messages.map { m -> m.toClientMessage() }}" }
                        return DialogExecutionReport(
                            dialog.id,
                            true,
                            it.id,
                            botMessage.toMessage()
                        )
                    }
                }
            }

            DialogExecutionReport(dialog.id)
        } catch (e: Exception) {
            logger.error(e)
            DialogExecutionReport(dialog.id, true, errorMessage = e.message)
        } finally {
            userTimelineDAO.remove(PlayerId(playerId, PlayerType.user))
        }
    }

    private fun getPath(testPlan: TestPlan): String {
        val applicationId = testPlan.applicationId
        return applicationIdPathCache.get(applicationId) {
            botConfigurationDAO.getConfigurationById(testPlan.botApplicationConfigurationId)?.path ?: "/$applicationId"
        }
    }

    private fun ClientMessage.deepEquals(action: TestActionReport): Boolean {
        return action.messages.any {
            deepEquals(it.toClientMessage())
        }
    }

    private fun ClientMessage.deepEquals(message: ClientMessage): Boolean {
        return if (message is ClientSentence && this is ClientSentence) {
            message.copy(
                text = message.text?.trim(),
                messages = message.messages.map { it.copy(connectorType = ClientConnectorType.none) }.toMutableList()
            ) == copy(
                text = text?.trim(),
                messages = messages.map { it.copy(connectorType = ClientConnectorType.none) }.toMutableList())
        } else {
            message == this
        }
    }

}