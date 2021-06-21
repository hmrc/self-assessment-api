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

package router.controllers

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import support.IntegrationSpec


class DocumentationControllerISpec extends IntegrationSpec {

  val apiDefinitionJson: JsValue = Json.parse("""
    |{
    |  "scopes":[
    |    {
    |      "key":"read:self-assessment",
    |      "name":"View your Self Assessment information",
    |      "description":"Allow read access to self assessment data",
    |      "confidenceLevel": 200
    |    },
    |    {
    |      "key":"write:self-assessment",
    |      "name":"Change your Self Assessment information",
    |      "description":"Allow write access to self assessment data",
    |      "confidenceLevel": 200
    |    }
    |  ],
    |  "api":{
    |    "name":"Self Assessment (MTD)",
    |    "description":"An API for providing self assessment data and obtaining tax calculations",
    |    "context":"self-assessment",
    |    "versions":[
    |      {
    |        "version":"1.0",
    |        "status":"RETIRED",
    |        "endpointsEnabled":true
    |      },
    |      {
    |        "version":"2.0",
    |        "status":"BETA",
    |        "endpointsEnabled":true
    |      }
    |    ]
    |  }
    |}
  """.stripMargin)

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

  "a documentation request" must {
    "return the documentation for v1" in {
      val response: WSResponse = await(buildRequest("/api/conf/1.0/application.raml").get())
      response.status shouldBe Status.OK
      response.body[String] should startWith("#%RAML 1.0")
    }

    "return the documentation for v2" in {
      val response: WSResponse = await(buildRequest("/api/conf/2.0/application.raml").get())
      response.status shouldBe Status.OK
      response.body[String] should startWith("#%RAML 1.0")
    }
  }
}
