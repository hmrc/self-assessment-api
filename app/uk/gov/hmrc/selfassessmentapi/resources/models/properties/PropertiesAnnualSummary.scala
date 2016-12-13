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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.selfassessmentapi.resources.models._

case class PropertiesAnnualSummary(allowances: Option[Allowances],
                                   adjustments: Option[Adjustments],
                                   rentARoomRelief: Option[Amount],
                                   privateUseAdjustment: Option[Amount],
                                   balancingCharge: Option[Amount]) extends AnnualSummary

object PropertiesAnnualSummary {
  implicit val writes: Writes[PropertiesAnnualSummary] = Json.writes[PropertiesAnnualSummary]

  implicit val reads: Reads[PropertiesAnnualSummary] = (
    (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments] and
      (__ \ "rentARoomRelief").readNullable[Amount](positiveAmountValidator) and
      (__ \ "privateUseAdjustment").readNullable[Amount](positiveAmountValidator) and
      (__ \ "balancingCharge").readNullable[Amount](positiveAmountValidator)
    ) (PropertiesAnnualSummary.apply _)
}
