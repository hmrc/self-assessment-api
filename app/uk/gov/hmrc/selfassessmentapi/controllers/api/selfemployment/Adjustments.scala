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

package uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._

case class Adjustments(includedNonTaxableProfits: BigDecimal,
                       basisAdjustment: BigDecimal,
                       overlapReliefUsed: BigDecimal,
                       accountingAdjustment: BigDecimal,
                       averagingAdjustment: BigDecimal,
                       lossBroughtForward: BigDecimal,
                       outstandingBusinessIncome: BigDecimal)

object Adjustments extends JsonMarshaller[Adjustments] {
  implicit val writes = Json.writes[Adjustments]

  implicit val reads: Reads[Adjustments] = onlyFields(classOf[Adjustments].getDeclaredFields.map(_.getName)) andThen (
    (__ \ "includedNonTaxableProfits").read[BigDecimal](positiveAmountValidator("includedNonTaxableProfits")) and
      (__ \ "basisAdjustment").read[BigDecimal](amountValidator("basisAdjustment")) and
      (__ \ "overlapReliefUsed").read[BigDecimal](positiveAmountValidator("overlapReliefUsed")) and
      (__ \ "accountingAdjustment").read[BigDecimal](positiveAmountValidator("accountingAdjustment")) and
      (__ \ "averagingAdjustment").read[BigDecimal](amountValidator("averagingAdjustment")) and
      (__ \ "lossBroughtForward").read[BigDecimal](positiveAmountValidator("lossBroughtForward")) and
      (__ \ "outstandingBusinessIncome").read[BigDecimal](positiveAmountValidator("outstandingBusinessIncome"))
    ) (Adjustments.apply _)

  private def onlyFields(allowed: Seq[String]): Reads[JsObject] = {
    Reads.filter(
      ValidationError("Unknown adjustment")
    )(_.keys.forall(allowed.contains))
  }

  def example(id: Option[SourceId] = None) = Adjustments(
    includedNonTaxableProfits = BigDecimal(50.00),
    basisAdjustment = BigDecimal(20.10),
    overlapReliefUsed = BigDecimal(500.00),
    accountingAdjustment = BigDecimal(10.50),
    averagingAdjustment = BigDecimal(-400.99),
    lossBroughtForward = BigDecimal(10000.00),
    outstandingBusinessIncome = BigDecimal(50.00))
}
