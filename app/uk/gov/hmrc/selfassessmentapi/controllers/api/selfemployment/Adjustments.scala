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

case class Adjustments(includedNonTaxableProfits: Option[BigDecimal] = None,
                       basisAdjustment: Option[BigDecimal] = None,
                       overlapReliefUsed: Option[BigDecimal] = None,
                       accountingAdjustment: Option[BigDecimal] = None,
                       averagingAdjustment: Option[BigDecimal] = None,
                       lossBroughtForward: Option[BigDecimal] = None,
                       outstandingBusinessIncome: Option[BigDecimal] = None)

object Adjustments {
  implicit val writes = Json.writes[Adjustments]

  implicit val reads: Reads[Adjustments] = onlyFields(classOf[Adjustments].getDeclaredFields.map(_.getName)) andThen (
    (__ \ "includedNonTaxableProfits").readNullable[BigDecimal](positiveAmountValidator("includedNonTaxableProfits")) and
      (__ \ "basisAdjustment").readNullable[BigDecimal](amountValidator("basisAdjustment")) and
      (__ \ "overlapReliefUsed").readNullable[BigDecimal](positiveAmountValidator("overlapReliefUsed")) and
      (__ \ "accountingAdjustment").readNullable[BigDecimal](positiveAmountValidator("accountingAdjustment")) and
      (__ \ "averagingAdjustment").readNullable[BigDecimal](amountValidator("averagingAdjustment")) and
      (__ \ "lossBroughtForward").readNullable[BigDecimal](positiveAmountValidator("lossBroughtForward")) and
      (__ \ "outstandingBusinessIncome").readNullable[BigDecimal](positiveAmountValidator("outstandingBusinessIncome"))
    ) (Adjustments.apply _)

  private def onlyFields(allowed: Seq[String]): Reads[JsObject] = {
    Reads.filter(
      ValidationError("Unknown adjustment")
    )(_.keys.forall(allowed.contains))
  }

  val example = Adjustments(
    includedNonTaxableProfits = Some(BigDecimal(50.00)),
    basisAdjustment = Some(BigDecimal(20.10)),
    overlapReliefUsed = Some(BigDecimal(500.00)),
    accountingAdjustment = Some(BigDecimal(10.50)),
    averagingAdjustment = Some(BigDecimal(-400.99)),
    lossBroughtForward = Some(BigDecimal(10000.00)),
    outstandingBusinessIncome = Some(BigDecimal(50.00)))
}
