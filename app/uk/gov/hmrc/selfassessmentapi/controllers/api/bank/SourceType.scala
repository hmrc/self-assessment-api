/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.controllers.api.bank

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.SummaryTypes.Interests

object SourceType {
  object Banks extends SourceType {
    override val name = "banks"
    override val documentationName = "Banks"

    override def example(id: Option[SummaryId] = None): JsValue = Json.toJson(Bank.example(id))

    override val summaryTypes: Set[SummaryType] = Set(Interests)
    override val title = "Sample bank"

    override def description(action: String) = s"$action a bank"
    override val fieldDescriptions = Seq()
  }
}
