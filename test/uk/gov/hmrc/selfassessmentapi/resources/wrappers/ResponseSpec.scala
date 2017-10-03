/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.contexts.{Agent, FilingOnlyAgent, Individual}
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.AuthRequest

class ResponseSpec extends UnitSpec with TableDrivenPropertyChecks {
  "response filter" should {
    val fakeRequest = FakeRequest(Helpers.POST, "", FakeHeaders(), Json.obj())

    "return a BadRequest with a generic error if the response contains a 4xx error and the user is a FOA" in {
      implicit val authReq: AuthRequest[JsValue] =
        new AuthRequest[JsValue](FilingOnlyAgent(Some("agentCode"), Some("agentReference")), fakeRequest)

      new Response {
        override val status: Int = 409

        override def underlying: HttpResponse = HttpResponse(status)
      }.filter {
        case _ => Conflict
      } shouldBe BadRequest(Json.toJson(Errors.InvalidRequest))
    }

    "return the response unmodified if it contains a non-4xx error and the user is a FOA" in {
      implicit val authReq =
        new AuthRequest[JsValue](FilingOnlyAgent(Some("agentCode"), Some("agentReference")), fakeRequest)

      val result = new Response {
        override val status = 200
        override def underlying = HttpResponse(status, responseHeaders = Map("CorrelationId" -> Seq("7777777")))
      }.filter {
        case _ => Ok
      }
      result shouldBe Ok.withHeaders("X-CorrelationId" -> "7777777")
    }

    "return the response unmodified if it contains a 4xx error and the user is not a FOA" in {
      implicit val authReq: AuthRequest[JsValue] = new AuthRequest[JsValue](Individual, fakeRequest)

      new Response {
        override val status: Int = 409

        override def underlying: HttpResponse = HttpResponse(status)
      }.filter {
        case _ => Conflict
      } shouldBe Conflict
    }

    val errorMappings =
      Table(
        ("HTTP Status", "DES Error Codes", "SA API Error Code"),
        (400, Seq(INVALID_NINO), BadRequest(toJson(Errors.NinoInvalid))),
        (400, Seq(INVALID_PAYLOAD), BadRequest(toJson(Errors.InvalidRequest))),
        (400,
         Seq(NOT_FOUND_NINO,
             INVALID_INCOMESOURCEID,
             INVALID_BUSINESSID,
             INVALID_INCOME_SOURCE,
             INVALID_INCOMESOURCEID,
             INVALID_TYPE,
             INVALID_IDENTIFIER,
             INVALID_CALCID),
         NotFound),
        (400,
         Seq(INVALID_ORIGINATOR_ID, INVALID_DATE_FROM, INVALID_DATE_TO, INVALID_STATUS, INVALID_TAX_YEAR),
         InternalServerError(toJson(Errors.InternalServerError))),
        (403, Seq(INVALID_DATE_RANGE), InternalServerError(toJson(Errors.InternalServerError))),
        (403, Seq(NOT_UNDER_16), Forbidden(toJson(Errors.businessError(Errors.NotUnder16)))),
        (403, Seq(NOT_OVER_STATE_PENSION), Forbidden(toJson(Errors.businessError(Errors.NotOverStatePension)))),
        (403, Seq(MISSING_EXEMPTION_INDICATOR), BadRequest(toJson(Errors.badRequest(Errors.MissingExemptionIndicator)))),
        (403, Seq(MISSING_EXEMPTION_REASON), BadRequest(toJson(Errors.badRequest(Errors.MandatoryFieldMissing)))),
        (403, Seq.empty, NotFound),
        (404, Seq(NOT_FOUND_INCOME_SOURCE), NotFound),
        (404, Seq.empty, NotFound),
        (409, Seq(INVALID_PERIOD), BadRequest(toJson(Errors.badRequest(Errors.InvalidPeriod)))),
        (409, Seq(NOT_CONTIGUOUS_PERIOD), Forbidden(toJson(Errors.businessError(Errors.NotContiguousPeriod)))),
        (409, Seq(OVERLAPS_IN_PERIOD), Forbidden(toJson(Errors.businessError(Errors.OverlappingPeriod)))),
        (409, Seq(NOT_ALIGN_PERIOD), Forbidden(toJson(Errors.businessError(Errors.MisalignedPeriod)))),
        (409, Seq(BOTH_EXPENSES_SUPPLIED), BadRequest(toJson(Errors.badRequest(Errors.BothExpensesSupplied)))),
        (409, Seq(NOT_ALLOWED_CONSOLIDATED_EXPENSES), Forbidden(toJson(Errors.businessError(Errors.NotAllowedConsolidatedExpenses)))),
        (500, Seq(SERVER_ERROR), InternalServerError(toJson(Errors.InternalServerError))),
        (503, Seq(SERVICE_UNAVAILABLE), InternalServerError(toJson(Errors.InternalServerError))),
        (500, Seq.empty, InternalServerError(toJson(Errors.InternalServerError)))
      )

    "map DES error codes to SA API error codes" in {
      implicit val authReq = new AuthRequest[JsValue](Individual, fakeRequest)

      def assertMapping(httpCode: Int, desErrCode: Option[DesErrorCode], apiErr: Result): Unit =
        new Response {
          override val status: Int = httpCode
          override def underlying: HttpResponse =
            HttpResponse(
              status,
              Some(Json.parse(s"""
                                        |{
                                        |  "code": "${desErrCode.getOrElse("")}",
                                        |  "reason": ""
                                        |}
                            """.stripMargin))
            )
        }.filter(PartialFunction.empty) shouldBe apiErr

      forAll(errorMappings) { (httpCode, desErrs, apiErr) =>
        if (desErrs.isEmpty)
          assertMapping(httpCode, None, apiErr)
        else
          desErrs.foreach { desErr =>
            assertMapping(httpCode, Some(desErr), apiErr)
          }
      }
    }

    "return Forbidden with a list of errors if the response contains a 409 with multiple period validation errors" in {
      implicit val authReq = new AuthRequest[JsValue](Agent(Some("agentCode"), Some("agentReference")), fakeRequest)

      new Response {
        override val status: Int = 409

        override def underlying: HttpResponse =
          HttpResponse(
            status,
            Some(Json.parse(s"""
                             |{
                             |  "failures": [
                               |              {
                               |                "code": "NOT_CONTIGUOUS_PERIOD",
                               |                "reason": ""
                               |              },
                               |              {
                               |                "code": "NOT_ALIGN_PERIOD",
                               |                "reason": ""
                               |              }
                             |              ]
                             |}
                            """.stripMargin))
          )
      }.filter(PartialFunction.empty) shouldBe Forbidden(
        Json.toJson(Errors.businessError(Seq(Errors.NotContiguousPeriod, Errors.MisalignedPeriod))))
    }

  }
}
