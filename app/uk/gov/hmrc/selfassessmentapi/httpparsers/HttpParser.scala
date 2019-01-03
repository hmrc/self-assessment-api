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

package uk.gov.hmrc.selfassessmentapi.httpparsers

import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.Errors.{DesError, Error, ErrorWrapper, InvalidRequest, NinoInvalid, NoSubmissionDataExists, SelfEmploymentIDInvalid, ServerError, ServiceUnavailable, TaxYearInvalid}

import scala.util.{Failure, Success, Try}

trait HttpParser extends Status {

  implicit class httpResponseOps(resp: HttpResponse){
    def jsonOpt: Option[JsValue] = {
      Try(resp.json) match {
        case Success(json) => Some(json)
        case Failure(_) => None
      }
    }
  }

  implicit class optionalJsonOps(optJson: Option[JsValue]){
    def validate[T : Reads]: JsResult[T] = optJson match {
      case Some(json) =>
        JsDefined(json).validateOpt[T] match {
          case JsSuccess(Some(j2), _) => JsSuccess(j2)
          case JsSuccess(None, _) => JsError()
          case err @ JsError(_) => err
        }
      case None => JsError()
    }
  }

  private val multipleErrorReads: Reads[Seq[DesError]] = (__ \ "failures").read[Seq[DesError]]

  def parseErrors(arg: JsValue): ErrorWrapper = {

    val errorWrapper = multipleErrorReads.reads(arg).get.map(_.code).map(desErrorToMtdError)
    if(errorWrapper.contains(ServerError))
      ErrorWrapper(ServerError, None)
    else
      ErrorWrapper(InvalidRequest, Some(multipleErrorReads.reads(arg).get.map(_.code).map(desErrorToMtdError)))
  }

  private val desErrorToMtdError: Map[String, Error] = Map(
    "NOT_FOUND" -> NoSubmissionDataExists,
    "INVALID_IDTYPE" -> ServerError,
    "INVALID_IDVALUE" -> NinoInvalid,
    "SERVER_ERROR" -> ServerError,
    "INVALID_TAXYEAR" -> TaxYearInvalid,
    "SERVICE_UNAVAILABLE" -> ServiceUnavailable,
    "INVALID_INCOMESOURCEID" -> SelfEmploymentIDInvalid,
    "INVALID_INCOMESOURCETYPE" -> ServerError
  )
}
