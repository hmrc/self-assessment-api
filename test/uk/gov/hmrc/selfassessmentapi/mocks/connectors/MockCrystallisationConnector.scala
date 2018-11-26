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

package uk.gov.hmrc.r2.selfassessmentapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.connectors.CrystallisationConnector
import uk.gov.hmrc.r2.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.models.crystallisation.CrystallisationRequest
import uk.gov.hmrc.r2.selfassessmentapi.resources.utils.ObligationQueryParams

trait MockCrystallisationConnector extends Mock {
  _: Suite =>

  val mockCrystallisationConnector = mock[CrystallisationConnector]

  object MockCrystallisationConnector {

    def intentToCrystallise(nino: Nino, taxYear: TaxYear, requestTS: String) = {
      when(mockCrystallisationConnector.intentToCrystallise(eqTo(nino), eqTo(taxYear))(any(), any()))
    }

    def crystallise(nino: Nino, taxYear: TaxYear, request: CrystallisationRequest) = {
      when(mockCrystallisationConnector.crystallise(eqTo(nino), eqTo(taxYear), eqTo(request))(any(), any()))
    }

    def get(nino: Nino, queryParams: ObligationQueryParams) = {
      when(mockCrystallisationConnector.get(eqTo(nino), eqTo(queryParams))(any(), any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCrystallisationConnector)
  }
}
