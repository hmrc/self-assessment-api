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

package support.functional

import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder}
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest.matchers.should.Matchers._
import play.api.Application
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import support.wiremock.{WireMockHelpers, WireMockVerify}


trait FunctionalSyntax extends Given with When with Then {

  implicit val app: Application

  def Given() = new GivenWord
}

/**
  * Responsible for setting up WireMock stubs
  */
trait Given extends WireMockHelpers {
  _: FunctionalSyntax =>

  class GivenWord {

    def theClientIsAuthorised: GivenWord = {
      stubPost("/auth/authorise", 200, Json.arr())
      this
    }

    def get(url: String): StubRequest = {
      new StubRequest(partialStubGet(url))
    }

    def post(url: String): StubRequest = {
      new StubRequest(partialStubPost(url))
    }

    def put(url: String): StubRequest = {
      new StubRequest(partialStubPut(url))
    }

    def And() = new GivenWord

    def When() = new HttpVerb
  }

  class StubRequest(request: MappingBuilder) {
    def returns(response: ResponseDefinitionBuilder): GivenWord = {
      stubFor(request.willReturn(response))
      new GivenWord()
    }
  }
}

/**
  * Responsible for building and staging a http request
  *
  * Calling Then() executes the request
  */
trait When extends HttpRequest {
  _: FunctionalSyntax =>

  class WhenWord(request: HttpRequest) {

    def withHeaders(headers: (String, String)*): WhenWord = {
      new WhenWord(requestWithHeaders(request, headers))
    }

    def Then(): ThenWord = {
      val response = await(request.execute)
      new ThenWord(request.wsRequest, response)
    }
  }

  class HttpVerb {
    def get(url: String): WhenWord = {
      new WhenWord(httpGet(url))
    }

    def post(url: String): RequiresBody = {
      new RequiresBody(httpPost(url))
    }

    def put(url: String): RequiresBody = {
      new RequiresBody(httpPut(url))
    }

    class RequiresBody(request: HttpRequest) {
      def withBody(body: String): WhenWord = {
        new WhenWord(requestWithBody(request, body))
      }

      def withBody(body: JsValue): WhenWord = withBody(body.toString())
    }
  }
}

/**
  * Responsible for assertions against the response and WireMock stubs
  */
trait Then {
  _: FunctionalSyntax =>

  class ThenWord(request: WSRequest, response: WSResponse) {

    def statusIs(status: Int): ThenWord = {
      withClue(s"expected ${request.url} to return $status, but got ${response.status}\n") {
        response.status shouldBe status
      }
      this
    }

    def contentTypeIs(contentType: String): ThenWord = {
      val contentTypeHeader = response.header(CONTENT_TYPE)
      require(contentTypeHeader.isDefined, s"expected response from ${request.url} to contain $CONTENT_TYPE header, but did not\n")

      contentTypeHeader.get shouldBe contentType
      this
    }

    def bodyIs(body: String): ThenWord = {
      response.body shouldBe body
      this
    }

    def bodyIs(body: JsValue): ThenWord = {
      bodyIs(body.toString())
    }

    def containsHeaders(headers: (String, String)*): ThenWord = {
      headers.foreach{ case (name, headerValue) =>
        val header = response.header(name)
        withClue(s"Expected response from ${request.url} to contain header $name, but didn't"){
          header.isDefined shouldBe true
        }
        withClue(s"Expected response from ${request.url} to contain header $name with value $headerValue"){
          header.get shouldBe headerValue
        }
      }
      this
    }

    def verify(mockToVerify: Verify): ThenWord = this
  }

  def mockFor(url: String) = new Verify(url)
}

/**
  * Verification methods against WireMock stubs setup during the Given() state
  */
class Verify(url: String) extends WireMockVerify {

  private val mock: LoggedRequest = getMockFor(url)

  def receivedHeaders(headers: (String, String)*): Verify = {
    headers.map { header =>
      verifyHeaderExists(mock, header)
    }
    this
  }
}
