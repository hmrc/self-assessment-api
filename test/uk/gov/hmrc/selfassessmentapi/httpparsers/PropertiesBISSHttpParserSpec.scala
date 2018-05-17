/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.fixtures.properties.PropertiesBISSFixture
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import play.api.http.Status._

class PropertiesBISSHttpParserSpec extends UnitSpec {

  val httpParser = new PropertiesBISSHttpParser {}

  val method = "test-method"
  val url = "test-url"

  val propertiesBISS = PropertiesBISSFixture.propertiesBISS()
  val propertiesBISSJson = PropertiesBISSFixture.propertiesBISSDESJson()

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

        result shouldBe Left(Errors.ServerError)
      }

      "DES returns a 500 with a ServerError error code" in {
        val responseBody = Json.obj("code" -> SERVER_ERROR)

        val response = HttpResponse(INTERNAL_SERVER_ERROR, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.ServerError)
      }

      "DES returns an unknown response code" in {
        val responseBody = Json.obj("code" -> "some code")

        val response = HttpResponse(999, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.ServerError)
      }

      "DES returns an empty response body" in {
        val response = HttpResponse(500, None)
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.ServerError)
      }
    }

    "return a NinoInvalid" when {
      "DES returns a 400 with a NinoInvalid error code" in {
        val responseBody = Json.obj("code" -> NINO_INVALID)

        val response = HttpResponse(BAD_REQUEST, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.NinoInvalid)
      }
    }

    "return a TaxYearInvalid" when {
      "DES returns a 400 with a TaxYearInvalid error code" in {
        val responseBody = Json.obj("code" -> TAX_YEAR_INVALID)

        val response = HttpResponse(BAD_REQUEST, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.TaxYearInvalid)
      }
    }

    "return a NinoNotFound" when {
      "DES returns a 404 with a NinoNotFound error code" in {
        val responseBody = Json.obj("code" -> NINO_NOT_FOUND)

        val response = HttpResponse(NOT_FOUND, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.NinoNotFound)
      }
    }

    "return a TaxYearNotFound" when {
      "DES returns a 404 with a TaxYearNotFound error code" in {
        val responseBody = Json.obj("code" -> TAX_YEAR_NOT_FOUND)

        val response = HttpResponse(NOT_FOUND, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.TaxYearNotFound)
      }
    }

    "return a NoSubmissionDataExists" when {
      "DES returns a 404 with a NoSubmissionDataExists error code" in {
        val responseBody = Json.obj("code" -> "NO_DATA_EXISTS")

        val response = HttpResponse(NOT_FOUND, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(NoSubmissionDataExists)
      }
    }

    "return a ServiceUnavailable" when {
      "DES returns a 503 with a ServiceUnavailable error code" in {
        val responseBody = Json.obj("code" -> "SERVICE_UNAVAILABLE")

        val response = HttpResponse(SERVICE_UNAVAILABLE, Some(responseBody))
        val result = httpParser.propertiesBISSHttpParser.read(method, url, response)

        result shouldBe Left(Errors.ServiceUnavailable)
      }
    }
  }
}
