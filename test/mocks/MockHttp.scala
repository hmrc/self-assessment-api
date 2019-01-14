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

package mocks

import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future


trait MockHttp extends Mock { _: Suite =>

  val mockHttp: HttpClient = mock[HttpClient]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  object MockHttp {
    def GET[T](url: String): OngoingStubbing[Future[T]] = {
      when(mockHttp.GET[T](eqTo(url))(any(), any(), any()))
    }

    def POST[I, O](url: String, body: I, headers: (String, String)*): OngoingStubbing[Future[O]] = {
      when(mockHttp.POST[I, O](eqTo(url), eqTo(body), eqTo(headers))(any(), any(), any(), any()))
    }

    def POST[I, O](url: String, body: I): OngoingStubbing[Future[O]] = {
      when(mockHttp.POST[I, O](eqTo(url), eqTo(body), any())(any(), any(), any(), any()))
    }

    def PUT[I, O](url: String, body: I): OngoingStubbing[Future[O]] = {
      when(mockHttp.PUT[I, O](eqTo(url), eqTo(body))(any(), any(), any(), any()))
    }
  }
}
