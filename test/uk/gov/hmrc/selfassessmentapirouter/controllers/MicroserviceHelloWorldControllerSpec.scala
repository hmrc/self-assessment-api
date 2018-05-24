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

package uk.gov.hmrc.selfassessmentapirouter.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.WithFakeApplication
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class MicroserviceHelloWorldControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")

  "GET /" should {
    "return 200" in {
      val controller = new MicroserviceHelloWorld()
      val result = controller.hello()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

}
