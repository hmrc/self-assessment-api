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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models._

case class FHLPropertiesAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                   otherCapitalAllowance: Option[BigDecimal] = None)

object FHLPropertiesAllowances {
  implicit val writes: Writes[FHLPropertiesAllowances] = Json.writes[FHLPropertiesAllowances]

  implicit val reads: Reads[FHLPropertiesAllowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "otherCapitalAllowance").readNullable[BigDecimal](nonNegativeAmountValidator)
    ) (FHLPropertiesAllowances.apply _)
}
