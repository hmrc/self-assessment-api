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

package support.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WireMockConfig {
  val wiremockHost = "localhost"
  val wiremockPort = 11111
  val wiremockUrl = s"http://$wiremockHost:$wiremockPort"
}

trait WireMock extends WireMockConfig
  with BeforeAndAfterAll
  with BeforeAndAfterEach { _: Suite =>

  private val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }
}
