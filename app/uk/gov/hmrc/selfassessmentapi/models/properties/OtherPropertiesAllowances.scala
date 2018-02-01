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

package uk.gov.hmrc.selfassessmentapi.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models._

case class OtherPropertiesAllowances(annualInvestmentAllowance: Option[Amount] = None,
                                     otherCapitalAllowance: Option[Amount] = None,
                                     costOfReplacingDomesticItems: Option[Amount] = None,
                                     zeroEmissionsGoodsVehicleAllowance: Option[Amount] = None,
                                     rentARoomExempt: Option[Amount] = None)

object OtherPropertiesAllowances {
  implicit val writes: Writes[OtherPropertiesAllowances] = Json.writes[OtherPropertiesAllowances]

  implicit val reads: Reads[OtherPropertiesAllowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "otherCapitalAllowance").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "costOfReplacingDomesticItems").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "zeroEmissionsGoodsVehicleAllowance").readNullable[Amount](nonNegativeAmountValidator)  and
      (__ \ "rentARoomExempt").readNullable[Amount](nonNegativeAmountValidator)
  )(OtherPropertiesAllowances.apply _)
}
