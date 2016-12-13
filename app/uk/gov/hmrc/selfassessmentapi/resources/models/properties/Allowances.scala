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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.Sum
import uk.gov.hmrc.selfassessmentapi.resources.models._

case class Allowances(annualInvestmentAllowance: Option[Amount] = None,
                      businessPremisesRenovationAllowance: Option[Amount] = None,
                      otherCapitalAllowance: Option[Amount] = None,
                      wearAndTearAllowance: Option[Amount] = None
                     ) {

  def total: BigDecimal = Sum(annualInvestmentAllowance, businessPremisesRenovationAllowance, otherCapitalAllowance, wearAndTearAllowance)
}

object Allowances {
  implicit val writes = Json.writes[Allowances]

  implicit val reads: Reads[Allowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "otherCapitalAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "wearAndTearAllowance").readNullable[Amount](positiveAmountValidator)
    ) (Allowances.apply _)
}
