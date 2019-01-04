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

package router.definition

import fixtures.SelfAssessmentApiDefinitionFixture._
import support.UnitSpec

class ApiDefinitionSpec extends UnitSpec {

  val apiDefinition: APIDefinition = selfAssessmentApiDefinition.api

  "APIDefinition" should {
    "throw an IllegalArgumentException" when {
      "name is empty" in {
        val ex = intercept[IllegalArgumentException](apiDefinition.copy(name = ""))
        ex.getMessage shouldBe "requirement failed: name is required"
      }

      "context is empty" in {
        val ex = intercept[IllegalArgumentException](apiDefinition.copy(context = ""))
        ex.getMessage shouldBe "requirement failed: context is required"
      }

      "description is empty" in {
        val ex = intercept[IllegalArgumentException](apiDefinition.copy(description = ""))
        ex.getMessage shouldBe "requirement failed: description is required"
      }

      "versions is empty" in {
        val ex = intercept[IllegalArgumentException](apiDefinition.copy(versions = Seq()))
        ex.getMessage shouldBe "requirement failed: at least one version is required"
      }

      "versions are not unique" in {
        val ex = intercept[IllegalArgumentException](apiDefinition.copy(versions = List(apiVersion_1, apiVersion_1)))
        ex.getMessage shouldBe "requirement failed: version numbers must be unique"
      }
    }
  }

  "APIVersion" should {
    "throw an IllegalArgumentException" when {
      "version is empty" in {
        val ex = intercept[IllegalArgumentException](apiVersion_1.copy(version = ""))
        ex.getMessage shouldBe "requirement failed: version is required"
      }
    }
  }
}
