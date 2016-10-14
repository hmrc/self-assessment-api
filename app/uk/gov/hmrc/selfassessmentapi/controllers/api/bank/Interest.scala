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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.InterestType.InterestType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonMarshaller, _}
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson.enumFormat

object InterestType extends Enumeration {
  type InterestType = Value
  val Taxed, Untaxed = Value
  implicit val format = enumFormat(InterestType, Some("Bank interest type is invalid"))
}

case class Interest(id: Option[String] = None, `type`: InterestType, amount: BigDecimal)

object Interest extends JsonMarshaller[Interest] {

  implicit val writes = Json.writes[Interest]

  implicit val reads: Reads[Interest] = (
    Reads.pure(None) and
      (__ \ "type").read[InterestType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (Interest.apply _)

  override def example(id: Option[SummaryId]) = Interest(id, InterestType.Taxed, BigDecimal(1000.00))
}
