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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.ErrorCode

trait JsonSpec extends UnitSpec {

  def roundTripJson[T](json: T)(implicit format: Format[T]): Unit = {
    val write = Json.toJson(json)
    val read = write.validate[T]
    read.asOpt shouldEqual Some(json)
  }

  def assertJsonIs[T](input: T, expectedOutput: T)(implicit format: Format[T]): Unit = {
    val write = Json.toJson(input)
    val read = write.validate[T]

    read.asOpt shouldEqual Some(expectedOutput)
  }

  def assertValidationPasses[T](o: T)(implicit format: Format[T]): Unit = {
    val json = Json.toJson(o)
    json.validate[T].fold(
      invalid => fail(invalid.seq.mkString(", ")),
      valid =>  valid shouldEqual o
    )
  }

  def assertValidationErrorWithCode[T : Format](obj: T, path: String, error: ErrorCode): Unit =
    assertValidationErrorsWithCode[T](Json.toJson(obj), Map(path -> error))

  def assertValidationErrorWithMessage[T : Format](obj: T, path: String, message: String): Unit =
    assertValidationErrorsWithMessage[T](Json.toJson(obj), Map(path -> message))

  def assertValidationErrorsWithCode[T : Format](value: JsValue, pathAndCode: Map[String, ErrorCode]): Unit = {
    val expectedError = pathAndCode.map { case (path, code) => path -> Seq(ValidationError("", Seq(code))) }.toSeq

    value.validate[T].asEither match {
      case Left(errs) => errs.map {
        case (p, e) => p.toString -> e.map(x => ValidationError("", x.args))
      } should contain theSameElementsAs expectedError
      case Right(_) =>
        fail(s"Provided object passed json validation. Was expected to fail for the paths: ${expectedError}")
    }
  }

  def assertValidationErrorsWithMessage[T : Format](value: JsValue, pathAndMessage: Map[String, String]): Unit = {
    val expectedError = pathAndMessage.map { case (path, msg) => path -> Seq(ValidationError(msg)) }

    value.validate[T].asEither match {
      case Left(errs) => errs.map {
        case (p, e) => p.toString -> e.map(x => ValidationError(x.message))
      } should contain theSameElementsAs expectedError
      case Right(_) =>
        fail(s"Provided object passed json validation. Was expected to fail for the paths: ${expectedError.keys}")
    }
  }
}
