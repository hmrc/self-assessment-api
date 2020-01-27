/*
 * Copyright 2020 HM Revenue & Customs
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

package router.resources

import support.ResourceSpec

class BaseResourceSpec extends ResourceSpec {

  class Setup {
    val resource: BaseResource = new BaseResource(controllerComponents, mockAuthConnector)
    mockAuthAction
  }

  "toSimpleHeaders" should {
    "generate headers with X-Content-Type-Options included" when {
      "called with any list of headers" in new Setup {

        val testHeaders: Map[String, Seq[String]] = Map(
          "Some-Random-Header" -> Seq("ValA"),
          "Some-Other-Random-Header" -> Seq("ValB")
        )

        val result = resource.toSimpleHeaders(testHeaders).toMap

        result.get("X-Content-Type-Options") shouldBe Some("nosniff")

      }
    }
    "generate headers with Content-Type included" when {
      "called with any list of headers" in new Setup {

        val testHeaders: Map[String, Seq[String]] = Map(
          "Some-Random-Header" -> Seq("ValA"),
          "Some-Other-Random-Header" -> Seq("ValB")
        )

        val result = resource.toSimpleHeaders(testHeaders).toMap

        result.get("Content-Type") shouldBe Some("application/json")

      }
    }
  }

}
