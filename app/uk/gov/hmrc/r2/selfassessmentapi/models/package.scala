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

package uk.gov.hmrc.r2.selfassessmentapi

import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Reads}
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{FHL, Other}

import scala.io.{Codec, Source}
import scala.util.Try

package object models {

  //type BigDecimal = BigDecimal
  type SourceId = String
  type PropertyId = String
  type PeriodId = String
  type SummaryId = String
  type ValidationErrors = Seq[(JsPath, Seq[ValidationError])]

  private val MAX_AMOUNT =    BigDecimal("99999999999999.98")

  //added to correct the max amounts for R2 fields
  private val MAX_AMOUNT_R2 = BigDecimal("99999999999.99")

  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val amountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(
      ValidationError("amount should be a number less than 99999999999999.98 with up to 2 decimal places", ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places
    */
  val nonNegativeAmountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(ValidationError("amounts should be a non-negative number less than 99999999999999.98 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places for release 2
    */
  val nonNegativeAmountValidatorR2: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(ValidationError("amounts should be a non-negative number less than 99999999999.99 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= MAX_AMOUNT_R2)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places for release 2
    */
  val nonNegativeIncomeValidatorR2: Reads[Income] = Reads
    .of[Income]
    .filter(
      ValidationError("amounts should be a non-negative number less than 99999999999.99 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT)
    )(income => income.amount >= 0 && income.amount.scale < 3 && income.amount <= MAX_AMOUNT_R2)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places for release 2
    */
  val nonNegativeFhlExpenseValidatorR2: Reads[FHL.Expense] = Reads
    .of[FHL.Expense]
    .filter(
      ValidationError("Income amounts should be a non-negative number less than 99999999999.99 with up to 2 decimal places",
        ErrorCode.INVALID_MONETARY_AMOUNT)
    )(expense => expense.amount >= 0 && expense.amount.scale < 3 && expense.amount <= MAX_AMOUNT_R2)

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places for release 2
    */
  val nonNegativeOtherExpenseValidatorR2: Reads[Other.Expense] = Reads
    .of[Other.Expense]
    .filter(
      ValidationError("Income amounts should be a non-negative number less than 99999999999.99 with up to 2 decimal places",
        ErrorCode.INVALID_MONETARY_AMOUNT)
    )(expense => expense.amount >= 0 && expense.amount.scale < 3 && expense.amount <= MAX_AMOUNT_R2)


  /**
    * Asserts that amounts must have a maximum of two decimal places
    */
  val positiveOrNegativeAmountValidator: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(
      ValidationError("amount should be a number between -99999999999.99 and 99999999999.99 with up to 2 decimal places",
        ErrorCode.INVALID_MONETARY_AMOUNT))(
        amount => amount >= -99999999999.99 && amount.scale < 3 && amount <= 99999999999.99)


  val sicClassifications: Try[Seq[String]] =
    for {
      lines <- {
        Try(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("SICs.txt"))(Codec.UTF8))
          .recover {
            case ex =>
              Logger.warn(s"Error loading SIC classifications file SICs.txt: ${ex.getMessage}")
              throw ex
          }
      }
    } yield lines.getLines().toIndexedSeq


  val postcodeValidator: Reads[String] = Reads
    .of[String]
    .filter(ValidationError("postalCode must match \"^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$\"",
      ErrorCode.INVALID_POSTCODE))(postcode =>
      postcode.matches("^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$"))

  val commencementDateValidator: Reads[LocalDate] = Reads
    .of[LocalDate]
    .filter(
      ValidationError("commencement date should be today or in the past", ErrorCode.DATE_NOT_IN_THE_PAST)
    )(date => date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()))

  def lengthIsBetween(minLength: Int, maxLength: Int): Reads[String] =
    Reads
      .of[String]
      .map(_.trim)
      .filter(
        ValidationError(s"field length must be between $minLength and $maxLength characters",
          ErrorCode.INVALID_FIELD_LENGTH))(name => name.length <= maxLength && name.length >= minLength)


  implicit class Trimmer(reads: Reads[String]) {
    def trim: Reads[String] = reads.map(_.trim)
  }

  implicit class NullableTrimmer(reads: Reads[Option[String]]) {
    def trimNullable: Reads[Option[String]] = reads.map(_.map(_.trim))
  }

  /**
    * Asserts that amounts must be non-negative and have a maximum of two decimal places
    */
  val nonNegativeAmountValidatorForCharitableGivings: Reads[BigDecimal] = Reads
    .of[BigDecimal]
    .filter(ValidationError("amounts should be a non-negative number less than 10000000000.00 with up to 2 decimal places",
      ErrorCode.INVALID_MONETARY_AMOUNT))(
      amount => amount >= 0 && amount.scale < 3 && amount <= 10000000000.00)

}
