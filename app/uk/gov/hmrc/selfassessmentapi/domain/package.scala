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

package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.domain.CountryCodes.{apply => _}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.UkCountryCodes.{apply => _}


package object domain {

  import play.api.data.validation.ValidationError

  type SourceId = String
  type SummaryId = String
  type LiabilityId = String
  type ValidationErrors = Seq[(JsPath, Seq[ValidationError])]

  def lengthValidator = Reads.of[String].filter(ValidationError("field length exceeded the max 100 chars", MAX_FIELD_LENGTH_EXCEEDED))(_.length <= 100)

  def positiveAmountValidator(fieldName: String) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName should be non-negative number up to 2 decimal values",
    INVALID_MONETARY_AMOUNT))(x => x >= 0 && x.scale < 3)

  def amountValidator(fieldName: String) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName should be a number up to 2 decimal values",
    INVALID_MONETARY_AMOUNT))(x => x.scale < 3)

  def maxAmountValidator(fieldName: String, maxAmount: BigDecimal) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName cannot be greater than $maxAmount",
    MAX_MONETARY_AMOUNT))(_ <= maxAmount)

}
