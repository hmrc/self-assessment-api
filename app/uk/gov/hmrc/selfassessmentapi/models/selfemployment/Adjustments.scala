/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.{amountValidator, nonNegativeAmountValidator}

case class Adjustments(includedNonTaxableProfits: Option[BigDecimal] = None,
                       basisAdjustment: Option[BigDecimal] = None,
                       overlapReliefUsed: Option[BigDecimal] = None,
                       accountingAdjustment: Option[BigDecimal] = None,
                       averagingAdjustment: Option[BigDecimal] = None,
                       lossBroughtForward: Option[BigDecimal] = None,
                       outstandingBusinessIncome: Option[BigDecimal] = None,
                       balancingChargeBPRA: Option[BigDecimal] = None,
                       balancingChargeOther: Option[BigDecimal] = None,
                       goodsAndServicesOwnUse: Option[BigDecimal] = None,
                       overlapProfitCarriedForward: Option[BigDecimal] = None,
                       overlapProfitBroughtForward: Option[BigDecimal] = None,
                       lossCarriedForwardTotal: Option[BigDecimal] = None,
                       cisDeductionsTotal: Option[BigDecimal] = None,
                       taxDeductionsFromTradingIncome: Option[BigDecimal] = None,
                       class4NicProfitAdjustment: Option[BigDecimal] = None)

object Adjustments {

  implicit val writes: Writes[Adjustments] = Json.writes[Adjustments]

  implicit val reads: Reads[Adjustments] = (
      (__ \ "includedNonTaxableProfits").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "basisAdjustment").readNullable[BigDecimal](amountValidator) and
      (__ \ "overlapReliefUsed").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "accountingAdjustment").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "averagingAdjustment").readNullable[BigDecimal](amountValidator) and
      (__ \ "lossBroughtForward").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "outstandingBusinessIncome").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "balancingChargeBPRA").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "balancingChargeOther").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "goodsAndServicesOwnUse").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "overlapProfitCarriedForward").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "overlapProfitBroughtForward").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "lossCarriedForwardTotal").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "cisDeductionsTotal").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "taxDeductionsFromTradingIncome").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "class4NicProfitAdjustment").readNullable[BigDecimal](nonNegativeAmountValidator)
    ) (Adjustments.apply _)
}
