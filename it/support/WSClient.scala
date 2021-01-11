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

package support

import play.api.Application
import play.api.libs.ws.{WSRequest, WSClient => PlayWSClient}

trait WSClient {

  implicit val app: Application
  val port: Int

  private val ws = app.injector.instanceOf[PlayWSClient]

  def buildRequest(uri: String): WSRequest = ws.url(s"http://localhost:$port$uri")
}
