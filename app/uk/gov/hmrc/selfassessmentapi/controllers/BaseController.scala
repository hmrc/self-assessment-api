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
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ValidationErrors, ErrorCode}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._

import scala.concurrent.Future

trait BaseController
  extends uk.gov.hmrc.play.microservice.controller.BaseController {

  val context: String

  val notFound = NotFound(Json.toJson(ErrorNotFound))

  def hc(request: Request[Any]): HeaderCarrier =
    HeaderCarrier.fromHeadersAndSession(request.headers, None)

  override protected def withJsonBody[T](f: (T) => Future[Result])(
    implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]) =
    request.body.validate[T] match {
      case JsSuccess(payload, _) => f(payload)
      case JsError(errors) => Future.successful(BadRequest(Json.toJson(invalidRequest(errors))))
    }

  def invalidRequest(errors: ValidationErrors) =
    InvalidRequest(ErrorCode.INVALID_REQUEST, "Validation failed", invalidPartsSeq(errors))

  def invalidRequest(message: String) =
    ErrorBadRequest(ErrorCode.INVALID_REQUEST, message)

  private def invalidPartsSeq(errors: ValidationErrors): Seq[InvalidPart] = {
    for {
      (path, errSeq) <- errors
      error <- errSeq
    } yield {
      InvalidPart(extractErrorCode(error), error.message, path.toString())
    }
  }

  private def extractErrorCode(error: ValidationError): ErrorCode = {
    error.args.headOption
      .filter(_.isInstanceOf[ErrorCode])
      .map(_.asInstanceOf[ErrorCode])
      .getOrElse(ErrorCode.INVALID_FIELD)
  }
}
