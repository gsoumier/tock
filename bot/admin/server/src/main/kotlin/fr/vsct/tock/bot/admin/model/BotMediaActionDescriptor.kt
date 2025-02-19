/*
 * Copyright (C) 2017/2019 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.connector.media.MediaActionDescriptor
import fr.vsct.tock.bot.connector.media.MediaMessageType
import fr.vsct.tock.bot.connector.media.MediaMessageType.card
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.Translator

data class BotMediaActionDescriptor(val title: I18nLabel, var url: String? = null) : BotMediaMessageDescriptor {

    constructor(desc: MediaActionDescriptor) : this(Translator.saveIfNotExists(desc.title), desc.url)

    override val type: MediaMessageType = card

    override fun toDescriptor(): MediaActionDescriptor = MediaActionDescriptor(I18nLabelValue(title), url)
}