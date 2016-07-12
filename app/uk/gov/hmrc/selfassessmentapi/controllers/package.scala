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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}

import scala.util.{Failure, Success, Try}
import scala.concurrent.Future

package object controllers {

  def validate[T](id: String, jsValue: JsValue)(implicit reads: Reads[T]): Either[ErrorResult, String] = {
    Try(jsValue.validate[T]) match {
      case Success(JsSuccess(payload, _)) => Right(id)
      case Success(JsError(errors)) => Left(ValidationErrorResult(errors))
      case Failure(e) => Left(GenericErrorResult(s"could not parse body due to ${e.getMessage}"))
    }
  }

  def validate[T, R](jsValue: JsValue)(f: T => Future[R])(implicit reads: Reads[T]): Either[ErrorResult, Future[R]] = {
    Try(jsValue.validate[T]) match {
      case Success(JsSuccess(payload, _)) => Right(f(payload))
      case Success(JsError(errors)) => Left(ValidationErrorResult(errors))
      case Failure(e) => Left(GenericErrorResult(s"could not parse body due to ${e.getMessage}"))
    }
  }
}
