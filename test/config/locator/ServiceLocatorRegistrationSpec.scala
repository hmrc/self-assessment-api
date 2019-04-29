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

package config.locator

import mocks.config.MockAppConfig
import mocks.config.locator.MockServiceLocatorConnector
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

class ServiceLocatorRegistrationSpec extends UnitSpec
  with MockServiceLocatorConnector
  with MockAppConfig {

  def run(): Unit = {
    new ServiceLocatorRegistration(
      config = mockAppConfig,
      serviceLocatorConnector = mockServiceLocatorConnector
    )
  }

  "On instantiation, the ServiceLocatorRegistration" should {
    "call the service locator connector to register the service" when {
      "service locator registration is enabled in config" in {

        MockAppConfig.registrationEnabled returns true

        run()

        verify(mockServiceLocatorConnector, times(1)).register(any[HeaderCarrier]())
      }
    }

    "not call the service locator connector" when {
      "service locator registration is disabled in config" in {

        MockAppConfig.registrationEnabled returns false

        run()

        verify(mockServiceLocatorConnector, times(0)).register(any[HeaderCarrier]())
      }
    }
  }
}
