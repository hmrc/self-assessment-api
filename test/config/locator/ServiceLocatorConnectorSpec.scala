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

package config.locator

import mocks.MockHttp
import mocks.config.MockAppConfig
import support.UnitSpec
import uk.gov.hmrc.api.domain.Registration
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ServiceLocatorConnectorSpec extends UnitSpec
  with MockAppConfig
  with MockHttp {

  trait Setup {
    val connector = new ServiceLocatorConnector(
      config = mockAppConfig,
      http = mockHttp
    )
    MockAppConfig.appNameForServiceLocator returns appName
    MockAppConfig.appUrl returns appUrl
    MockAppConfig.serviceLocatorUrl returns serviceLocatorUrl
  }

  lazy val appName = "test-app-name"
  lazy val appUrl = "test-app-url"
  lazy val serviceLocatorUrl = "test-service-locator-url"

  "register" should {

    val url = s"$serviceLocatorUrl/registration"
    val body = Registration(appName, appUrl, Some(Map("third-party-api" -> "true")))

    "return true" when {
      "the POST request succeeds" in new Setup {
        MockHttp.POST[Registration, HttpResponse](url, body, CONTENT_TYPE -> JSON)
          .returns(Future.successful(HttpResponse(200)))

        await(connector.register) shouldBe true
      }
    }

    "return false" when {
      "when an exception is returned from the HttpClient" in new Setup {
        MockHttp.POST[Registration, HttpResponse](url, body, CONTENT_TYPE -> JSON)
          .returns(Future.failed(testException))

        await(connector.register) shouldBe false
      }
    }
  }
}
