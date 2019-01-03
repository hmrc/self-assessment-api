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

package uk.gov.hmrc.r2.selfassessmentapi.models

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models.Validation._
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class ValidationSpec extends JsonSpec {
  case class Foo(a: Int, b: Int)

  object Foo {
    implicit val writes: Writes[Foo] = Json.writes[Foo]

    implicit val reads: Reads[Foo] = (
      (__ \ "a").read[Int](max(2)) and
        (__ \ "b").read[Int](max(5))
    )(Foo.apply _)
      .validate(
        Seq(Validation(JsPath \ "a", (foo: Foo) => foo.a > foo.b, ValidationError("a should be greater than b")),
            Validation(JsPath \ "b", (foo: Foo) => foo.a + foo.b == 5, ValidationError("a + b should be 5"))))
  }

  "validate" should {
    "report multiple validation errors" in {
      assertValidationErrorsWithMessage[Foo](Json.parse("""{ "a": 1, "b": 3 }"""),
                                             Map("/a" -> Seq("a should be greater than b"),
                                                 "/b" -> Seq("a + b should be 5")))
    }
  }
}
