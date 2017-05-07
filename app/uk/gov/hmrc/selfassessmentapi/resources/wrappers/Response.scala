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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.Errors

trait Response {
  val logger: Logger = Logger(this.getClass)

  def underlying: HttpResponse

  def json: JsValue = underlying.json

  val status: Int = underlying.status

  private def logResponse(): Unit =
    logger.error(s"DES error occurred with status code ${underlying.status} and body ${underlying.body}")

  def filter(f: Int => Result)(implicit context: AuthContext): Result =
    status / 100 match {
      case 4 if context.isFOA =>
        logResponse()
        BadRequest(Json.toJson(Errors.InvalidRequest))
      case 4 | 5 =>
        logResponse()
        f(status)
      case _ => f(status)
    }
}
