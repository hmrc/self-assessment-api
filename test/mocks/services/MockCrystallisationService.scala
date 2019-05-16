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

package mocks.services

import mocks.Mock
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import play.api.libs.json.JsValue
import play.api.mvc.Request
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import router.services.CrystallisationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockCrystallisationService extends Mock { _: Suite =>
  val mockCrystallisationService = mock[CrystallisationService]

  object MockCrystallisationService {

    def post(body: JsValue): OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockCrystallisationService.post(eqTo(body))(any[HeaderCarrier](), any[Request[_]]()))
    }

    def postEmpty: OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockCrystallisationService.postEmpty(any[HeaderCarrier](), any[Request[_]]()))
    }

    def get(): OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockCrystallisationService.get()(any[HeaderCarrier](), any[Request[_]]()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCrystallisationService)
  }

}
