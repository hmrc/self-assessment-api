/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.config.MockAppConfig
import router.definition.APIStatus.APIStatus
import support.UnitSpec
import router.constants.Versions._

class SelfAssessmentApiDefinitionSpec extends UnitSpec
  with MockAppConfig {

  class Setup {
    val saApiDefinition = new SelfAssessmentApiDefinition(mockAppConfig)
  }

  "buildAPIStatus" should {

    Seq(
      "ALPHA" -> APIStatus.ALPHA,
      "BETA" -> APIStatus.BETA,
      "STABLE" -> APIStatus.STABLE,
      "DEPRECATED" -> APIStatus.DEPRECATED,
      "RETIRED" -> APIStatus.RETIRED,
      "any other string" -> APIStatus.ALPHA
    ).foreach { case (input, output) =>
      s"return $output when provided with $input from config" in new Setup {
        MockAppConfig.apiStatus("1.0")
          .returns(input)

        val apiStatus: APIStatus = saApiDefinition.buildAPIStatus(VERSION_1)
        apiStatus shouldBe output
      }
    }
  }
}