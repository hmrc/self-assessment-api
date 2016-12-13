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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.CountryCodes.{apply => _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkCountryCodes.{apply => _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.AmountHolder

import scala.math.BigDecimal.RoundingMode

package object api {

  type SourceId = String
  type Location = String
  type PeriodId = String
  type SummaryId = String
  type LiabilityId = String
  type LiabilityCalculationErrorId = String
  type ValidationErrors = Seq[(JsPath, Seq[ValidationError])]

  def lengthValidator = Reads.of[String].filter(ValidationError("field length exceeded the max 100 chars", MAX_FIELD_LENGTH_EXCEEDED))(_.length <= 100)

  def positiveAmountValidator(fieldName: String) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName should be non-negative number up to 2 decimal values",
    INVALID_MONETARY_AMOUNT))(x => x >= 0 && x.scale < 3)

  def amountValidator(fieldName: String) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName should be a number up to 2 decimal values",
    INVALID_MONETARY_AMOUNT))(x => x.scale < 3)

  def maxAmountValidator(fieldName: String, maxAmount: BigDecimal) = Reads.of[BigDecimal].filter(ValidationError(s"$fieldName cannot be greater than $maxAmount",
    MAX_MONETARY_AMOUNT))(_ <= maxAmount)

  object Sum {
    def apply(values: Option[BigDecimal]*) = values.flatten.sum
  }

  object Total {
    def apply(values: Seq[AmountHolder]) = values.map(_.amount).sum
  }

  object CapAt {
    def apply(n: Option[BigDecimal], cap: BigDecimal): Option[BigDecimal] = n map {
      case x if x > cap => cap
      case x => x
    }

    def apply(n: BigDecimal, cap: BigDecimal): BigDecimal = apply(Some(n), cap).get
  }

  object PositiveOrZero {
    def apply(n: BigDecimal): BigDecimal = n match {
      case x if x > 0 => x
      case _ => 0
    }
  }

  object ValueOrZero {
    def apply(maybeValue: Option[BigDecimal]): BigDecimal = maybeValue.getOrElse(0)
  }

  object RoundDown {
    def apply(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.DOWN)
  }

  object RoundUp {
    def apply(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.UP)
  }

  object FlooredAt {
    def apply(one: BigDecimal, two: BigDecimal) = if(one >= two) one else two
  }

  object RoundDownToEven {
    def apply(number: BigDecimal) = number - (number % 2)
  }

  object RoundUpToPennies {
    def apply(n: BigDecimal) = n.setScale(2, RoundingMode.UP)
  }

  object RoundDownToPennies  {
    def apply(n: BigDecimal) = n.setScale(2, RoundingMode.DOWN)
  }

}

