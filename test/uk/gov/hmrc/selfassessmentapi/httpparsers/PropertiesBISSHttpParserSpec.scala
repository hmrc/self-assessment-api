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

package uk.gov.hmrc.selfassessmentapi.httpparsers

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.fixtures.properties.PropertiesBISSFixture
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import play.api.http.Status._
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertiesBISS

class PropertiesBISSHttpParserSpec extends UnitSpec {

  val httpParser = new PropertiesBISSHttpParser {}

  val method = "test-method"
  val url = "test-url"

  val propertiesBISS: PropertiesBISS = PropertiesBISSFixture.propertiesBISS()
  val propertiesBISSJson: JsObject = PropertiesBISSFixture.propertiesBISSDESJson()

  "PropertiesBISSHttpParser" should {
    "return a PropertiesBISS model" when {
      "DES return a 200 with a valid response body" in {
        val response = HttpResponse(OK, Some(propertiesBISSJson))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Right(propertiesBISS)
      }
    }

    "return a ServerError" when {
      "DES returns a 200 with an invalid response body" in {
        val response = HttpResponse(OK, Some(Json.obj("invalid" -> "response")))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServerError, None))
      }

      "DES returns a 500 with a ServerError error code" in {
        val responseBody = Json.obj("code" -> SERVER_ERROR)

        val response = HttpResponse(INTERNAL_SERVER_ERROR, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServerError, None))
      }

      "DES returns an unknown response code" in {
        val responseBody = Json.obj("code" -> "some code")

        val response = HttpResponse(999, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServerError, None))
      }

      "DES returns an empty response body" in {
        val response = HttpResponse(500, None)
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServerError, None))
      }

      "DES returns a 200 with an empty response body" in {
        val response = HttpResponse(200, None)
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServerError, None))
      }
    }

    "return a NinoInvalid" when {
      "DES returns a 400 with a NinoInvalid error code" in {
        val responseBody = Json.obj("code" -> DesErrorCode.INVALID_IDVALUE)

        val response = HttpResponse(BAD_REQUEST, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.NinoInvalid, None))
      }
    }

    "return a TaxYearInvalid" when {
      "DES returns a 400 with a TaxYearInvalid error code" in {
        val responseBody = Json.obj("code" -> DesErrorCode.INVALID_TAX_YEAR)

        val response = HttpResponse(BAD_REQUEST, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.TaxYearInvalid, None))
      }
    }

    "return a not found" when {
      "DES returns a 404 with a not found error code" in {
        val responseBody = Json.obj("code" -> "NOT_FOUND")

        val response = HttpResponse(NOT_FOUND, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(NoSubmissionDataExists, None))
      }
    }

    "return a ServiceUnavailable" when {
      "DES returns a 503 with a ServiceUnavailable error code" in {
        val responseBody = Json.obj("code" -> "SERVICE_UNAVAILABLE")

        val response = HttpResponse(SERVICE_UNAVAILABLE, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.ServiceUnavailable, None))
      }
    }

    "return a multiple errors" when {
      "DES returns a multiple errors" in {
        val responseBody = Json.parse(
          """
            |{
            |   "failures": [
            |        {
            |            "code": "NOT_FOUND",
            |            "reason": "Submission has not passed validation. Invalid parameter idType."
            |        },
            |        {
            |            "code": "INVALID_IDVALUE",
            |            "reason": "Submission has not passed validation. Invalid parameter idValue."
            |        }
            |    ]
            |}
          """.stripMargin)

        val response = HttpResponse(BAD_REQUEST, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(ErrorWrapper(Errors.InvalidRequest, Some(Seq(NoSubmissionDataExists, NinoInvalid))))
      }
    }
  }
}
