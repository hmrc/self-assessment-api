/*
 * Copyright 2017 HM Revenue & Customs
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


import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Result
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.ErrorCode
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode.{SERVICE_UNAVAILABLE => SERVICE_NOT_AVAILABLE, _}
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, MultiDesError}

object Errors {

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

  object Error {
    private val logger = Logger(Error.getClass)

    @deprecated(message = "This will potentially need to be redesigned so that http status codes returned to TPVs " +
      "are driven by des error codes and not by http status codes returned by the DES")
    def from(json: JsValue): JsValue = {
      json.asOpt[DesError].map { err =>
        toJson(fromDesError(err))
      }.orElse {
        json.asOpt[MultiDesError].map { err =>
          toJson(businessError(err.failures.map(fromDesError)))
        }
      }.getOrElse {
        logger.error(s"Error received from DES does not match what we are expecting.")
        toJson(Error("UNKNOWN_ERROR", "Unknown error", Some("")))
      }
    }

    private def fromDesError(err: DesError): Error = {
      Error(err.code.toString, err.reason, Some(""))
    }

    import play.api.http.Status._
    // the line below blocks importing InternalServerError from Results
    import play.api.mvc.Results.{InternalServerError => _, _}

    private def fromDesError2(error: DesError): (Int, Option[Error]) = {

      error.code match {
        case INVALID_NINO => BAD_REQUEST -> Some(NinoInvalid)
        case INVALID_BUSINESSID => NOT_FOUND -> None
        case INVALID_TAX_YEAR => INTERNAL_SERVER_ERROR -> Some(InternalServerError)
        case INVALID_ORIGINATOR_ID => INTERNAL_SERVER_ERROR -> Some(InternalServerError)
        case INVALID_PAYLOAD => BAD_REQUEST -> Some(InvalidRequest)
        case NOT_FOUND_TAX_YEAR => NOT_FOUND -> None
        case NOT_FOUND_NINO => NOT_FOUND -> None
        case NOT_FOUND_BUSINESS_ID => NOT_FOUND -> None
        case SERVER_ERROR => INTERNAL_SERVER_ERROR -> Some(InternalServerError)
        case SERVICE_NOT_AVAILABLE => INTERNAL_SERVER_ERROR -> Some(InternalServerError)
        case _ => INTERNAL_SERVER_ERROR -> Some(InternalServerError)
      }
    }

    def from2(json: JsValue) =
      json.asOpt[DesError].map { err =>
        val (code, content) = fromDesError2(err)
        content.fold[Result](Status(code))(err => Status(code)(toJson(err)))
      }.orElse {
        json.asOpt[MultiDesError].map { err =>
          val businessErrors = err.failures.map(fromDesError2).unzip._2
          Status(BAD_REQUEST)(toJson(businessError(businessErrors.map(_.getOrElse(UnknownError)))))
        }
      }.getOrElse {
        logger.error(s"Error received from DES does not match what we are expecting.")
        Status(INTERNAL_SERVER_ERROR)(toJson(UnknownError))
      }

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

  object NinoInvalid extends Error("NINO_INVALID", "The provided Nino is invalid", None)
  object InvalidRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object InvalidPeriod extends Error("INVALID_PERIOD", "Periods should be contiguous and have no gaps between one another", Some(""))
  object ClientNotSubscribed extends Error("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD", None)
  object AgentNotAuthorized extends Error("AGENT_NOT_AUTHORIZED", "The agent is not authorized", None)
  object AgentNotSubscribed extends Error("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services", None)
  object BadToken extends Error("UNAUTHORIZED", "Bearer token is missing or not authorized", None)
  object BadRequest extends Error("INVALID_REQUEST", "Invalid request", None)
  object InternalServerError extends Error("INTERNAL_SERVER_ERROR", "An internal server error occurred", None)
  object UnknownError extends Error("UNKNOWN_ERROR", "Unknown error", Some(""))


  def badRequest(validationErrors: ValidationErrors) = BadRequest(flattenValidationErrors(validationErrors), "Invalid request")
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
      case _ => Error("UNMAPPED_PLAY_ERROR", playError.message, Some(errorPath))
    }
  }


}
