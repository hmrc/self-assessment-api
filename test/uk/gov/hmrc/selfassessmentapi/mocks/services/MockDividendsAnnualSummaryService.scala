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

package uk.gov.hmrc.r2.selfassessmentapi.mocks.services

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.models.dividends.Dividends
import uk.gov.hmrc.r2.selfassessmentapi.services.DividendsAnnualSummaryService

trait MockDividendsAnnualSummaryService extends Mock {
  _: Suite =>

  val mockDividendsAnnualSummaryService = mock[DividendsAnnualSummaryService]

  object MockDividendsAnnualSummaryService {
    def retrieveAnnualSummary(nino: Nino, taxYear: TaxYear) = {
      when(mockDividendsAnnualSummaryService.retrieveAnnualSummary(eqTo(nino), eqTo(taxYear))(any()))
    }

    def updateAnnualSummary(nino: Nino, taxYear: TaxYear, newDividends: Dividends) = {
      when(mockDividendsAnnualSummaryService.updateAnnualSummary(eqTo(nino), eqTo(taxYear), eqTo(newDividends))(any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDividendsAnnualSummaryService)
  }
}
