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

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._

trait JsonSpec extends UnitSpec {

  def roundTripJson[T](json: T)(implicit format: Format[T]) = {
    val write = Json.toJson(json)
    val read = write.validate[T]
    read.asOpt shouldEqual Some(json)
  }

  def assertValidationPasses[T](o: T)(implicit format: Format[T]): Unit = {
    val json = Json.toJson(o)(format)
    json.validate[T](format).fold(
      invalid => fail(invalid.seq.mkString(", ")),
      valid =>  valid shouldEqual o
    )
  }

  def assertValidationError[T](o: T, expectedErrors: Map[String, ErrorCode])(implicit format: Format[T]): Unit = {
    assertValidationError(o, expectedErrors, s"Validation errors expected for fields: ${expectedErrors.toSeq}, but no validation errors were returned")
  }

  def assertValidationError[T](o: T, expectedErrors: Map[String, ErrorCode], failureMessage: String)(implicit format: Format[T]): Unit = {
    val json = Json.toJson(o)(format)
    assertValidationError[T](json, expectedErrors, failureMessage)
  }

  def assertValidationError[T](json: JsValue, expectedErrors: Map[String, ErrorCode], failureMessage: String)(implicit format: Format[T]): Unit = {
    json.validate[T](format).fold(
      invalid =>  invalid.flatMap(x => x._2.map(error => x._1.toString -> error.args.head)) should contain theSameElementsAs expectedErrors,
      valid => fail(failureMessage)
    )
  }

}
