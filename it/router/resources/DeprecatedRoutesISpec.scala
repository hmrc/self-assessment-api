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

package router.resources

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import router.errors.ErrorCode
import support.IntegrationSpec
import support.stubs.AuthStub

class DeprecatedRoutesISpec extends IntegrationSpec {

  val gone: JsValue = ErrorCode.resourceGone.asJson

  trait Test {
    val nino = "AA123456B"

    val selfEmploymentId = "XKIS00000000988"

    val fromDate = "2018-04-06"
    val toDate = "2019-04-05"

    def acceptHeader: String

    def uri: String

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "hitting a deprecated route" should {
    "return 410" when {
      "the route is GET UK Property" in new Test {
        override def uri: String = s"/ni/$nino/uk-properties"

        override def acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is POST a tax calculation" in new Test {
        override def uri: String = s"/ni/$nino/calculations"

        override def acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.post(Json.obj()))
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET tax calculation" in new Test {
        override def uri: String = s"/ni/$nino/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET tax calculation messages" in new Test {
        override def uri: String = s"/ni/$nino/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2/validation-messages"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET all self employment business" in new Test {
        override def uri: String = s"/ni/$nino/self-employments"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET a self employment business" in new Test {
        override def uri: String = s"/ni/$nino/self-employments/$selfEmploymentId"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET self-employment business obligations" in new Test {
        override def uri: String = s"/ni/$nino/self-employments/$selfEmploymentId/obligations"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET self-employment business obligations with query parameters" in new Test {
        override def uri: String = s"/ni/$nino/self-employments/$selfEmploymentId/obligations?from=$fromDate&to=$toDate"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET End of Period Statement Obligations for a Self-Employment Business" in new Test {
        override def uri: String = s"/ni/$nino/self-employments/$selfEmploymentId/end-of-period-statements/obligations"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET End of Period Statement Obligations for a Self Employment Business with query parameters" in new Test {
        override def uri: String = s"/ni/$nino/self-employments/$selfEmploymentId/end-of-period-statements/obligations?from=$fromDate&to=$toDate"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET Crystallisation Obligations" in new Test {
        override def uri: String = s"/ni/$nino/crystallisation/obligations"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }

      "the route is GET Crystallisation Obligations with query parameters" in new Test {
        override def uri: String = s"/ni/$nino/crystallisation/obligations?from=$fromDate&to=$toDate"

        override def acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe GONE
        response.json shouldBe gone
      }
    }
  }
}
