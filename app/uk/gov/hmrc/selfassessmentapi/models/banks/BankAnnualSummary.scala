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

package uk.gov.hmrc.selfassessmentapi.models.banks

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.nonNegativeAmountValidator

case class BankAnnualSummary(taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal])

object BankAnnualSummary {
  implicit val writes: Writes[BankAnnualSummary] = Json.writes[BankAnnualSummary]

  implicit val reads: Reads[BankAnnualSummary] = (
    (__ \ "taxedUkInterest").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "untaxedUkInterest").readNullable[BigDecimal](nonNegativeAmountValidator)
  ) (BankAnnualSummary.apply _)
}
