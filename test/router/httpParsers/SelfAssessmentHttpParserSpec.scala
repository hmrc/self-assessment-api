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

package router.httpParsers

import play.api.http.Status
import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse

class SelfAssessmentHttpParserSpec extends UnitSpec {

  val httpParser: SelfAssessmentHttpParser = new SelfAssessmentHttpParser

  "SelfAssessmentHttpParser" should {
    "return a response" when {

      val testJson = Json.toJson(s"""{"testKey":"testValue"}""")

      "self-assessment returns a 200 (OK) response" in {
        val response = HttpResponse(Status.OK, testJson.toString())
        val result  = httpParser.read("GET", "/", response).value

        result shouldBe response
        result.status shouldBe Status.OK
        result.body shouldBe testJson.toString
      }
      "self-assessment returns a 500 (InternalServerError)" in {
        val response = HttpResponse(Status.INTERNAL_SERVER_ERROR, testJson.toString())
        val result = httpParser.read("GET", "/any-url", response).value

        result shouldBe response
        result.status shouldBe Status.INTERNAL_SERVER_ERROR
        result.body shouldBe testJson.toString
      }
      "self-assessment returns a 400 (BadRequest)" in {
        val response = HttpResponse(Status.BAD_REQUEST, testJson.toString())
        val result = httpParser.read("GET", "/any/url", response).value

        result shouldBe response
        result.status shouldBe Status.BAD_REQUEST
        result.body shouldBe testJson.toString
      }
    }
  }

}
