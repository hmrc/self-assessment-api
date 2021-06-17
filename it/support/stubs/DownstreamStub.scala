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

package support.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsValue
import support.wiremock.WireMockMethods

object DownstreamStub extends WireMockMethods {

  def onSuccess(method: HTTPMethod,
                uri: String,
                status: Int,
                body: JsValue,
                queryParams: Map[String, String] = Map.empty,
                requestHeaders: Map[String, String] = Map.empty,
                responseHeaders: Map[String, String] = Map.empty): StubMapping = {
    when(method = method, uri = uri, queryParams = queryParams, headers = requestHeaders)
      .thenReturn(status = status, body, headers = responseHeaders)
  }
}
