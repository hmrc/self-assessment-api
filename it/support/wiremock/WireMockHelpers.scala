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

package support.wiremock

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsValue

trait WireMockHelpers {

  def aResponse: ResponseDefinitionBuilder = WireMock.aResponse()

  def stubGet(url: String, status: Int, body: JsValue): StubMapping = {
    stubGet(url, status, Some(body.toString()))
  }

  def stubGet(url: String, status: Int, body: Option[String] = None): StubMapping = {
    stubFor(get(urlPathMatching(url)).willReturn(
      body.fold{
        aResponse
          .withStatus(status)
      }{
        aResponse
          .withStatus(status)
          .withBody(_)
      }
    ))
  }

  def partialStubGet(url: String): MappingBuilder = get(urlEqualTo(url))

  def stubPost(url: String, status: Int, body: JsValue): StubMapping = {
    stubPost(url, status, Some(body.toString()))
  }

  def stubPost(url: String, status: Int, body: Option[String] = None): StubMapping = {
    stubFor(post(urlPathMatching(url)).willReturn(
      body.fold{
        aResponse
          .withStatus(status)
      }{
        aResponse
          .withStatus(status)
          .withBody(_)
      }
    ))
  }

  def partialStubPost(url: String): MappingBuilder = post(urlPathMatching(url))

  def partialStubPut(url: String): MappingBuilder = put(urlPathMatching(url))

  implicit class responseBuilderOps(response: ResponseDefinitionBuilder){
    def withBody(json: JsValue): ResponseDefinitionBuilder = response.withBody(json.toString())
  }
}
