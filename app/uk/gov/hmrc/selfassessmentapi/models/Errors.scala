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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.ErrorCode
import play.api.libs.functional.syntax._
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode.DesErrorCode

object Errors {

  val NINO_INVALID = "NINO_INVALID"
  val TAX_YEAR_INVALID = "TAX_YEAR_INVALID"
  val NINO_NOT_FOUND = "NINO_NOT_FOUND"
  val TAX_YEAR_NOT_FOUND = "TAX_YEAR_NOT_FOUND"
  val SERVER_ERROR = "SERVER_ERROR"

  implicit val errorDescWrites: Writes[Error] = Json.writes[Error]
  implicit val badRequestWrites: Writes[BadRequest] = new Writes[BadRequest] {
    override def writes(req: BadRequest): JsValue = {
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
    }
  }

  implicit val businessErrorWrites: Writes[BusinessError] = new Writes[BusinessError] {
    override def writes(req: BusinessError) =
      Json.obj("code" -> req.code, "message" -> req.message, "errors" -> req.errors)
  }

  implicit val internalServerErrorWrites: Writes[InternalServerError] = new Writes[InternalServerError] {
    override def writes(req: InternalServerError): JsValue =
      Json.obj("code" -> req.code, "message" -> req.message)
  }

  case class Error(code: String, message: String, path: Option[String])

  object ErrorCode {
    val reads: Reads[Option[DesErrorCode]] = (__ \ "code").readNullable[DesErrorCode]

    def unapply(arg: Option[JsValue]): Option[DesErrorCode] = {
      arg match {
        case Some(json) => reads.reads(json).fold(_ => None, valid => valid)
        case _ => None
      }
    }
  }


  case class DesError(code: String, reason: String)

  object DesError {
    implicit val reads: Reads[DesError] = Json.reads[DesError]
  }

  case class BadRequest(errors: Seq[Error], message: String) {
    val code = "INVALID_REQUEST"
  }

  case class BusinessError(errors: Seq[Error], message: String) {
    val code = "BUSINESS_ERROR"
  }

  case class InternalServerError(message: String) {
    val code = "INTERNAL_SERVER_ERROR"
  }

  object NinoInvalid extends Error(NINO_INVALID, "The provided Nino is invalid", None)
  object NinoNotFound extends Error(NINO_NOT_FOUND, "The remote endpoint has indicated that no data can be found for the nino.", None)
  object TaxYearInvalid extends Error(TAX_YEAR_INVALID, "Invalid tax year", None)
  object TaxYearNotFound extends Error(TAX_YEAR_NOT_FOUND, "The remote endpoint has indicated that no data can be found for the tax year.", None)
  object InvalidRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object BothExpensesSupplied extends Error("BOTH_EXPENSES_SUPPLIED", "Elements: expenses and consolidatedElements cannot be both specified at the same time", None)
  object NotAllowedConsolidatedExpenses extends Error("NOT_ALLOWED_CONSOLIDATED_EXPENSES", "The submission contains consolidated expenses but the accumulative turnover amount exceeds the threshold", Some(""))
  object InvalidPeriod extends Error("INVALID_PERIOD", "The period 'from' date should come before the 'to' date", Some(""))
  object NotUnder16 extends Error("NOT_UNDER_16", "The Individual's age is equal to or greater than 16 years old on the 6th April of current tax year.", Some("/nonFinancials/class4NicInfo/exemptionCode"))
  object NotOverStatePension extends Error("NOT_OVER_STATE_PENSION", "The Individual's age is less than their State Pension age on the 6th April of current tax year.", Some("/nonFinancials/class4NicInfo/exemptionCode"))
  object MissingExemptionIndicator extends Error("INVALID_VALUE", "Exemption code must be present only if the exempt flag is set to true", Some("/nonFinancials/class4NicInfo"))
  object MandatoryFieldMissing extends Error("MANDATORY_FIELD_MISSING", "Exemption code value must be present if the exempt flag is set to true", Some("/nonFinancials/class4NicInfo"))
  object NotContiguousPeriod extends Error("NOT_CONTIGUOUS_PERIOD", "Periods should be contiguous.", Some(""))
  object OverlappingPeriod extends Error("OVERLAPPING_PERIOD", "Period overlaps with existing periods.", Some(""))
  object MisalignedPeriod extends Error("MISALIGNED_PERIOD", "Period is not within the accounting period.", Some(""))
  object ClientNotSubscribed extends Error("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD", None)
  object AgentNotAuthorized extends Error("AGENT_NOT_AUTHORIZED", "The agent is not authorized", None)
  object AgentNotSubscribed extends Error("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services", None)
  object BadToken extends Error("UNAUTHORIZED", "Bearer token is missing or not authorized", None)
  object BadRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object InternalServerError extends Error("INTERNAL_SERVER_ERROR", "An internal server error occurred", None)
  object NotFinalisedDeclaration extends Error("NOT_FINALISED", "The statement cannot be accepted without a declaration it is finalised.", Some("/finalised"))
  object PeriodicUpdateMissing extends Error("PERIODIC_UPDATE_MISSING", "End-of-period statement cannot be accepted until all periodic updates have been submitted.", None)
  object InvalidDateRange extends Error("INVALID_DATE_RANGE", "The start date must be the same day or before the from date.", None)
  object InvalidDateRange_2 extends Error("INVALID_DATE_RANGE", "The 'To' date must be after the 'From' date", None)
  object InvalidStartDate extends Error("INVALID_START_DATE", "The start date must be on or after 6th April 2017", None)
  object InvalidDate extends Error("INVALID_DATE", "The format of the 'From' or 'To' date is invalid", None)
  object EarlySubmission extends Error("EARLY_SUBMISSION", "You cannot submit a statement before the end of your accounting period.", None)
  object NonMatchingPeriod extends Error("NON_MATCHING_PERIOD", "You cannot submit your end-of-period statement for a period that does not match your accounting period.", None)
  object RequiredEndOfPeriodStatement extends Error("REQUIRED_END_OF_PERIOD_STATEMENT", "Cannot submit intent to crystallisation without submitting End of Period Statement.", Some(""))
  object RequiredIntentToCrystallise extends Error("REQUIRED_INTENT_TO_CRYSTALLISE", "Crystallisation could occur only after an intent to crystallise is sent.", None)
  object InvalidTaxCalculationId extends Error("INVALID_TAX_CALCULATION_ID", "The calculation id should match the calculation id returned by the latest intent to crystallise.", Some("/calculationId"))
  object AlreadySubmitted extends Error("ALREADY_SUBMITTED", "You cannot submit a statement for the same accounting period twice", None)
  object ServerError extends Error(SERVER_ERROR, "An error has occurred", None)
  object ServiceUnavailable extends Error("SERVICE_UNAVAILABLE", "The server is currently unavailable", None)
  object NoSubmissionDataExists extends Error("NOT_FOUND", "The remote endpoint has indicated that no data can be found.", None)
  object SelfEmploymentIDNotFound extends Error("SELF_EMPLOYMENT_ID_NOT_FOUND", "The remote endpoint has indicated that no data can be found for the self-employment ID", None)
  object SelfEmploymentIDInvalid extends Error("SELF_EMPLOYMENT_ID_INVALID", "The provided self-employment ID is invalid", None)

  def badRequest(validationErrors: ValidationErrors) = BadRequest(flattenValidationErrors(validationErrors), "Invalid request")
  def badRequest(error: Error) = BadRequest(Seq(error), "Invalid request")
  def badRequest(message: String) = BadRequest(Seq.empty, message)

  def businessError(error: Error): BusinessError = businessError(Seq(error))
  def businessError(errors: Seq[Error]): BusinessError = BusinessError(errors, "Business validation error")

  private def flattenValidationErrors(validationErrors: ValidationErrors): Seq[Error] = {
    validationErrors.flatMap { validationError =>
      val (path, errors) = validationError
      errors.map { err =>
        err.args match {
          case Seq(head, _*) if head.isInstanceOf[ErrorCode] =>
            Error(head.asInstanceOf[ErrorCode].toString, err.message, Some(path.toString()))
          case _ => convertErrorMessageToCode(err, path.toString())
        }
      }
    }
  }

  /*
   * Converts a Play error without an error code into an Error that contains an error code
   * based on the content of the error message.
   */
  private def convertErrorMessageToCode(playError: ValidationError, errorPath: String): Error = {
    playError.message match {
      case "error.expected.jodadate.format" => Error("INVALID_DATE", "please provide a date in ISO format (i.e. YYYY-MM-DD)", Some(errorPath))
      case "error.path.missing" => Error("MANDATORY_FIELD_MISSING", "a mandatory field is missing", Some(errorPath))
      case "error.expected.numberformatexception" => numberFormatExceptionError(errorPath)
      case "error.expected.jsstring" => Error("INVALID_STRING_VALUE", "please provide a string field", Some(errorPath))
      case "error.expected.jsboolean" => Error("INVALID_BOOLEAN_VALUE", "please provide a valid boolean field", Some(errorPath))
      case _ => Error("UNMAPPED_PLAY_ERROR", playError.message, Some(errorPath))
    }
  }

  def numberFormatExceptionError(errorPath: String) = Error("INVALID_NUMERIC_VALUE", "please provide a numeric field", Some(errorPath))

  def businessJsonError(error: Errors.Error) = Json.toJson(Errors.businessError(error))

  def desErrorsToApiErrors(desErrors: Seq[JsValue]): Seq[JsValue] = desErrors.map(e => desErrorToApiError(e))

  def desErrorToApiError(jsValue: JsValue): JsValue =
    jsValue.validate[DesError] match {
      case JsSuccess(payload, _) => Json.toJson(Error(payload.code, payload.reason, None))
      case JsError(errors) => jsValue
    }
}
