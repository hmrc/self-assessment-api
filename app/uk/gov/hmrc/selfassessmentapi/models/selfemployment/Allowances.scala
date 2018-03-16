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
import uk.gov.hmrc.selfassessmentapi.models.nonNegativeAmountValidator

case class Allowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                      capitalAllowanceMainPool: Option[BigDecimal] = None,
                      capitalAllowanceSpecialRatePool: Option[BigDecimal] = None,
                      businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                      enhancedCapitalAllowance: Option[BigDecimal] = None,
                      allowanceOnSales: Option[BigDecimal] = None,
                      zeroEmissionGoodsVehicleAllowance: Option[BigDecimal] = None)

object Allowances {

  lazy val example = Allowances(annualInvestmentAllowance = Some(1000.00),
                                capitalAllowanceMainPool = Some(150.00),
                                capitalAllowanceSpecialRatePool = Some(5000.50),
                                businessPremisesRenovationAllowance = Some(600.00),
                                enhancedCapitalAllowance = Some(50.00),
                                allowanceOnSales = Some(3399.99),
                                zeroEmissionGoodsVehicleAllowance = Some(2020))

  implicit val writes = Json.writes[Allowances]

  implicit val reads: Reads[Allowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "capitalAllowanceMainPool").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "capitalAllowanceSpecialRatePool").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "enhancedCapitalAllowance").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "allowanceOnSales").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal](nonNegativeAmountValidator)
  )(Allowances.apply _)
}
