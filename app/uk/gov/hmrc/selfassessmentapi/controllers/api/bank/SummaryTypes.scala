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

import play.api.libs.json.{ Json, JsValue }
import uk.gov.hmrc.selfassessmentapi.controllers.api.{FullFieldDescription, PositiveMonetaryFieldDescription, SummaryId, SummaryType}

object SummaryTypes {
  case object Interests extends SummaryType {
    override val name = "interests"
    override val documentationName = "Interests"

    override def example(id: Option[SummaryId]): JsValue = Json.toJson(Interest.example(id))

    override val title = "Sample bank interest"

    override def description(action: String): String = s"$action a bank interest for the specified source"

    override val fieldDescriptions = Seq(
      FullFieldDescription("bank", "type", "Enum", s"Type of bank interest (one of the following: ${InterestType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("bank", "amount", "Interest income from UK banks and building societies, split by interest types - Taxed interest and Untaxed interest.")
    )
  }
}
