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

package uk.gov.hmrc.selfassessmentapi

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.AmountHolder
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorResult, GenericErrorResult, ValidationErrorResult}

import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode
import scala.util.{Failure, Success, Try}

package object resources {



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

  object Sum {
    def apply(values: Option[BigDecimal]*) = values.flatten.sum
  }

  object Total {
    def apply(values: Seq[AmountHolder]) = values.map(_.amount).sum
  }

  object CapAt {
    def apply(n: Option[BigDecimal], cap: BigDecimal): Option[BigDecimal] = n map {
      case x if x > cap => cap
      case x => x
    }

    def apply(n: BigDecimal, cap: BigDecimal): BigDecimal = apply(Some(n), cap).get
  }

  object PositiveOrZero {
    def apply(n: BigDecimal): BigDecimal = n match {
      case x if x > 0 => x
      case _ => 0
    }
  }

  object ValueOrZero {
    def apply(maybeValue: Option[BigDecimal]): BigDecimal = maybeValue.getOrElse(0)
  }

  object RoundDown {
    def apply(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.DOWN)
  }

  object RoundUp {
    def apply(n: BigDecimal): BigDecimal = n.setScale(0, BigDecimal.RoundingMode.UP)
  }

  object FlooredAt {
    def apply(one: BigDecimal, two: BigDecimal) = if(one >= two) one else two
  }

  object RoundDownToEven {
    def apply(number: BigDecimal) = number - (number % 2)
  }

  object RoundUpToPennies {
    def apply(n: BigDecimal) = n.setScale(2, RoundingMode.UP)
  }

  object RoundDownToPennies  {
    def apply(n: BigDecimal) = n.setScale(2, RoundingMode.DOWN)
  }
}
