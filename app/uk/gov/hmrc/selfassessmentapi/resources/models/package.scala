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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Reads}

package object models {

  type Amount = BigDecimal
  type SourceId = String
  type PropertyId = String
  type PeriodId = String
  type SummaryId = String
  type LiabilityId = String
  type LiabilityCalculationErrorId = String
  type ValidationErrors = Seq[(JsPath, Seq[ValidationError])]

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val amountValidator = Reads.of[Amount]
    .filter(ValidationError("amount should be a number with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT))(_.scale < 3)

  /**
    * Asserts that amounts must be positive and have a maximum of two decimal places
    */
  val positiveAmountValidator = Reads.of[Amount]
    .filter(ValidationError("amounts should be positive numbers with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT)
    )(amount => amount >= 0 && amount.scale < 3)
}
