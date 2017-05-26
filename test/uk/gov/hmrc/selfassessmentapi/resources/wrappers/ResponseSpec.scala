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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.selfassessmentapi.contexts.{Individual, FilingOnlyAgent}
import uk.gov.hmrc.selfassessmentapi.models.Errors
import uk.gov.hmrc.selfassessmentapi.resources.AuthRequest

class ResponseSpec extends UnitSpec {
  "response filter" should {
    val fakeRequest = FakeRequest(Helpers.POST, "", FakeHeaders(), Json.obj())

    "return a BadRequest with a generic error if the response contains a 4xx error and the user is a FOA" in {
      implicit val authReq = new AuthRequest[JsValue](FilingOnlyAgent, fakeRequest)

      new Response {
        override val status: Int = 409
        override def underlying: HttpResponse = HttpResponse(status)
      }.filter(_ => Conflict) shouldBe BadRequest(Json.toJson(Errors.InvalidRequest))
    }

    "return the response unmodified if it contains a non-4xx error and the user is a FOA" in {
      implicit val authReq = new AuthRequest[JsValue](FilingOnlyAgent, fakeRequest)

      new Response {
        override val status: Int = 200
        override def underlying: HttpResponse = HttpResponse(status)
      }.filter(_ => Ok) shouldBe Ok
    }

    "return the response unmodified if it contains a 4xx error and the user is not a FOA" in {
      implicit val authReq = new AuthRequest[JsValue](Individual, fakeRequest)

      new Response {
        override val status: Int = 409
        override def underlying: HttpResponse = HttpResponse(status)
      }.filter(_ => Conflict) shouldBe Conflict
    }

  }
}
