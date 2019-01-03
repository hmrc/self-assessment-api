/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.calculation

import play.api.data.validation.ValidationError
import play.api.libs.json.{Reads, _}
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, TaxYear}

case class CalculationRequest(taxYear: TaxYear)

object CalculationRequest {
  private val taxYearValidator: Reads[TaxYear] = Reads.of[TaxYear].filter(
            ValidationError("Tax Year must be in the form of YYYY-YY and equal or after 2017-18",
            ErrorCode.TAX_YEAR_INVALID))(taxYear => TaxYear.createTaxYear(taxYear.taxYear).isDefined)

  implicit val reads: Reads[CalculationRequest] =
    (__ \ "taxYear").read[TaxYear](taxYearValidator).map(taxYear => CalculationRequest(taxYear))

  implicit val writes :Writes[CalculationRequest] = Json.writes[CalculationRequest]
}
