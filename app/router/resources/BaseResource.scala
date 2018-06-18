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

package router.resources

import play.api.libs.json.JsValue
import play.api.mvc.Result
import router.auth.AuthorisedActions
import router.errors.{ErrorCode, IncorrectAPIVersion, SelfAssessmentAPIError, UnsupportedAPIVersion}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.util.{Success, Try}

trait BaseResource extends BaseController with AuthorisedActions {

  private[resources] def buildResponse(apiResponse: HttpResponse): Result = {
    Try(apiResponse.json) match {
      case Success(_: JsValue) =>
        new Status(apiResponse.status)(apiResponse.json)
          .withHeaders(toSimpleHeaders(apiResponse.allHeaders):_*)
      case _ =>
        new Status(apiResponse.status)
          .withHeaders(toSimpleHeaders(apiResponse.allHeaders): _*)
    }
  }

  private[resources] def buildErrorResponse(error: SelfAssessmentAPIError): Result = {
    error match {
      case IncorrectAPIVersion => NotAcceptable(ErrorCode.invalidAcceptHeader.asJson)
      case UnsupportedAPIVersion => NotFound(ErrorCode.notFound.asJson)
    }
  }

  private def toSimpleHeaders(headers: Map[String, Seq[String]]): Seq[(String, String)] = {
    headers.flatMap{ case (name, values) => values.map(name -> _)}.toSeq
  }
}
