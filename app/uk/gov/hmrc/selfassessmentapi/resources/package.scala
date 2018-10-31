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

package uk.gov.hmrc.selfassessmentapi

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Forbidden, InternalServerError}
import uk.gov.hmrc.selfassessmentapi.models.{
  PathValidationErrorResult,
  AuthorisationErrorResult,
  ErrorResult,
  Errors,
  GenericErrorResult,
  ValidationErrorResult
}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.Response

package object resources {

  val GovTestScenarioHeader = "Gov-Test-Scenario"

  def unhandledResponse(status: Int, logger: Logger): Result = {
    logger.warn(s"Unhandled response from DES. Status code: $status. Returning 500 to client.")
    InternalServerError(Json.toJson(Errors.InternalServerError("An internal server error occurred")))
  }

  def handleErrors(errorResult: ErrorResult): Result = errorResult match {
    case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
    case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
    case AuthorisationErrorResult(error) => Forbidden(Json.toJson(error))
    case PathValidationErrorResult(error) => BadRequest(Json.toJson(error))
  }

  type BusinessResult[T] = EitherT[Future, ErrorResult, T]

  object BusinessResult {

    def apply[T](eventuallyErrorOrResult: Future[Either[ErrorResult, T]]): BusinessResult[T] =
      new EitherT(eventuallyErrorOrResult)

    def apply[T](errorOrResult: Either[ErrorResult, T]): BusinessResult[T] =
      EitherT.fromEither(errorOrResult)

    def success[T](value: T): BusinessResult[T] = EitherT.fromEither(Right(value))

    def failure[T](error: ErrorResult): BusinessResult[T] = EitherT.fromEither(Left(error))

  }

  implicit class DesBusinessResult[R <: Response](result: BusinessResult[R]) {

    def onDesSuccess(handleSuccess: R => Result): Future[Result] = {
      for {
        desResponseOrError <- result.value
      } yield desResponseOrError match {
        case Left(errors) => handleErrors(errors)
        case Right(desResponse) => handleSuccess(desResponse)
      }
    }

  }

  def validateJson[T](json: JsValue)(implicit reads: Reads[T]): BusinessResult[T] =
    BusinessResult {
      for {
        errors <- json.validate[T].asEither.left
      } yield ValidationErrorResult(errors)
    }

  def validate[T](value: T)(validate: PartialFunction[T, Errors.Error]): BusinessResult[T] =
    if (validate.isDefinedAt(value)) BusinessResult.failure(PathValidationErrorResult(validate(value)))
    else BusinessResult.success(value)

  def authorise[T](value: T)(auth: PartialFunction[T, Errors.Error]): BusinessResult[T] =
    if (auth.isDefinedAt(value)) BusinessResult.failure(AuthorisationErrorResult(Errors.businessError(auth(value))))
    else BusinessResult.success(value)

  def execute[T](f: Unit => Future[T]): BusinessResult[T] =
    BusinessResult {
      for {
        result <- f(())
      } yield Right(result)
    }

  def validate[T, R](jsValue: JsValue)(f: T => Future[R])(implicit reads: Reads[T]): Future[Either[ErrorResult, R]] =
    jsValue.validate[T] match {
      case JsSuccess(payload, _) => f(payload).map(Right(_))
      case JsError(errors) => Future.successful(Left(ValidationErrorResult(errors)))
    }

  def correlationId(resp: Response): String = resp.underlying.header("CorrelationId").getOrElse("No CorrelationId returned")
}
