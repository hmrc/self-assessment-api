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

case class Allowances(annualInvestmentAllowance: BigDecimal,
                      capitalAllowanceMainPool: BigDecimal,
                      capitalAllowanceSpecialRatePool: BigDecimal,
                      businessPremisesRenovationAllowance: BigDecimal,
                      enhancedCapitalAllowance: BigDecimal,
                      allowancesOnSales: BigDecimal) {

  private val maxAnnualInvestmentAllowance = 200000

  def total = {
    Sum(CapAt(annualInvestmentAllowance, maxAnnualInvestmentAllowance), capitalAllowanceMainPool, capitalAllowanceSpecialRatePool,
      businessPremisesRenovationAllowance, enhancedCapitalAllowance, allowancesOnSales)
  }
}

object Allowances {

  lazy val example = Allowances(
    annualInvestmentAllowance = BigDecimal(1000.00),
    capitalAllowanceMainPool = BigDecimal(150.00),
    capitalAllowanceSpecialRatePool = BigDecimal(5000.50),
    businessPremisesRenovationAllowance = BigDecimal(600.00),
    enhancedCapitalAllowance = BigDecimal(50.00),
    allowancesOnSales = BigDecimal(3399.99))

  implicit val writes = Json.writes[Allowances]

  implicit val reads: Reads[Allowances] = onlyFields(classOf[Allowances].getDeclaredFields.map(_.getName)) andThen (
      (__ \ "annualInvestmentAllowance").read[BigDecimal](positiveAmountValidator("annualInvestmentAllowance")) and
      (__ \ "capitalAllowanceMainPool").read[BigDecimal](positiveAmountValidator("capitalAllowanceMainPool")) and
      (__ \ "capitalAllowanceSpecialRatePool").read[BigDecimal](positiveAmountValidator("capitalAllowanceSpecialRatePool")) and
      (__ \ "businessPremisesRenovationAllowance").read[BigDecimal](positiveAmountValidator("businessPremisesRenovationAllowance")) and
      (__ \ "enhancedCapitalAllowance").read[BigDecimal](positiveAmountValidator("enhancedCapitalAllowance")) and
      (__ \ "allowancesOnSales").read[BigDecimal](positiveAmountValidator("allowancesOnSales"))
    ) (Allowances.apply _)

  private def onlyFields(allowed: Seq[String]): Reads[JsObject] = {
    Reads.filter(
      ValidationError("Unknown adjustment")
    )(_.keys.forall(allowed.contains))
  }
}
