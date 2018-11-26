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

package uk.gov.hmrc.selfassessmentapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, TaxYear}

trait MockSelfEmploymentAnnualSummaryConnector extends Mock {
  _: Suite =>

  val mockSelfEmploymentAnnualSummaryConnector = mock[SelfEmploymentAnnualSummaryConnector]

  object MockSelfEmploymentAnnualSummaryConnector {
    def update(nino: Nino, id: SourceId, taxYear: TaxYear, update: SelfEmploymentAnnualSummary) = {
      when(mockSelfEmploymentAnnualSummaryConnector.update(eqTo(nino), eqTo(id), eqTo(taxYear), eqTo(update))(any(), any()))
    }

    def get(nino: Nino, id: SourceId, taxYear: TaxYear) = {
      when(mockSelfEmploymentAnnualSummaryConnector.get(eqTo(nino), eqTo(id), eqTo(taxYear))(any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfEmploymentAnnualSummaryConnector)
  }
}
