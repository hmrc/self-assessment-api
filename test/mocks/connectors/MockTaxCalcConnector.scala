/*
 * Copyright 2020 HM Revenue & Customs
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

package mocks.connectors

import mocks.Mock
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import play.api.mvc.Request
import router.connectors.TaxCalcConnector
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockTaxCalcConnector extends Mock { _: Suite =>

  val mockTaxCalcConnector = mock[TaxCalcConnector]

  object MockTaxCalcConnector {
    def get(uri: String): OngoingStubbing[Future[SelfAssessmentOutcome]] = {
      when(mockTaxCalcConnector.get(eqTo(uri))(any[HeaderCarrier](), any[Request[_]]()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTaxCalcConnector)
  }
}
