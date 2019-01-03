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

package router.controllers

import support.UnitSpec
import mocks.definition.MockSelfAssessmentApiDefinition
import play.api.test.FakeRequest
import fixtures.SelfAssessmentApiDefinitionFixture._


class DocumentationControllerSpec extends UnitSpec
  with MockSelfAssessmentApiDefinition {

  class Setup {
    val controller = new DocumentationController(
      selfAssessmentApiDefinition = mockSelfAssessmentApiDefinition
    )
  }

  "definition" should {
    "return a 200 with the Api definition in the request body" when {
      "a self assessment api definition is created successfully" in new Setup {
        MockSelfAssessmentApiDefinition.definition
          .returns(selfAssessmentApiDefinition)

        val result = controller.definition()(FakeRequest())
        status(result) shouldBe 200
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe selfAssessmentApiDefinitionJson
      }
    }
  }
}
