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
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import router.services.SavingsAccountService

import scala.concurrent.Future

trait MockSavingsAccountService extends Mock { _: Suite =>

  val mockSavingsAccountService: SavingsAccountService = mock[SavingsAccountService]

  object MockSavingsAccountService {

    def post(body: JsValue): OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockSavingsAccountService.post(eqTo(body))(any(), any()))
    }

    def get(): OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockSavingsAccountService.get()(any(), any()))
    }

    def put(body: JsValue) = {
      when(mockSavingsAccountService.put(eqTo(body))(any(), any()))
    }

  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSavingsAccountService)
  }
}
