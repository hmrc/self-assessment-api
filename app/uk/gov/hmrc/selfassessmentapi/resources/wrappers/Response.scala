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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.contexts.FilingOnlyAgent
import uk.gov.hmrc.selfassessmentapi.models.des.DesError
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode.{DesErrorCode, _}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, PeriodSummary}
import uk.gov.hmrc.selfassessmentapi.resources.AuthRequest

trait Response {
  val logger: Logger = Logger(this.getClass)

  def underlying: HttpResponse

  def json: JsValue = underlying.json

  val status: Int = underlying.status

  private def logResponse(): Unit =
    logger.error(s"DES error occurred with status code ${underlying.status} and body ${underlying.body}")

  def filter[A](f: PartialFunction[Int, Result])(implicit request: AuthRequest[A]): Result =
    status / 100 match {
      case 4 if request.authContext == FilingOnlyAgent =>
        logResponse()
        BadRequest(toJson(Errors.InvalidRequest))
      case 4 | 5 =>
        logResponse()
        (f orElse errorMapping)(status)
      case _ => (f orElse errorMapping)(status)
    }

  private def errorMapping: PartialFunction[Int, Result] = {
    case 400 if errorCodeIsIn(INVALID_NINO)    => BadRequest(toJson(Errors.NinoInvalid))
    case 400 if errorCodeIsIn(INVALID_PAYLOAD) => BadRequest(toJson(Errors.InvalidRequest))
    case 400
        if errorCodeIsIn(INVALID_BUSINESSID, INVALID_INCOME_SOURCE, INVALID_TYPE, INVALID_IDENTIFIER, INVALID_CALCID) =>
      NotFound
    case 400 if errorCodeIsIn(INVALID_PERIOD) => Forbidden(Json.toJson(Errors.businessError(Errors.InvalidPeriod)))
    case 400
        if errorCodeIsIn(INVALID_ORIGINATOR_ID,
                         INVALID_DATE_RANGE,
                         INVALID_DATE_FROM,
                         INVALID_DATE_TO,
                         INVALID_STATUS,
                         INVALID_TAX_YEAR) =>
      InternalServerError(toJson(Errors.InternalServerError))
    case 403                                       => NotFound
    case 404                                       => NotFound
    case 500 if errorCodeIsIn(SERVER_ERROR)        => InternalServerError(toJson(Errors.InternalServerError))
    case 503 if errorCodeIsIn(SERVICE_UNAVAILABLE) => InternalServerError(toJson(Errors.InternalServerError))
    case _                                         => InternalServerError(toJson(Errors.InternalServerError))
  }

  def errorCodeIsIn(errorCodes: DesErrorCode*): Boolean =
    json.asOpt[DesError].exists(errorCode => errorCodes.contains(errorCode.code))
}

object Response {
  def periodsExceeding(maxPeriodTimeSpan: Int)(summary: PeriodSummary): Boolean =
    summary.from.plusDays(maxPeriodTimeSpan).isAfter(summary.to)
}
