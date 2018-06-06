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

package support

import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneServerPerSuite
import play.api.Application
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import support.functional.FunctionalSyntax
import support.wiremock.WireMock

trait IntegrationSpec extends WordSpec
  with OneServerPerSuite
  with WireMock
  with Matchers
  with Status
  with HeaderNames
  with MimeTypes
  with FakeApplicationConfig
  with FunctionalSyntax {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeApplicationConfig)
    .build()

}
