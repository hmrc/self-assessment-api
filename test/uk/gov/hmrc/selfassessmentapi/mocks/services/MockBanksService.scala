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
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceId
import uk.gov.hmrc.r2.selfassessmentapi.models.banks.Bank
import uk.gov.hmrc.r2.selfassessmentapi.services.BanksService

trait MockBanksService extends Mock { _: Suite =>

  val mockBanksService = mock[BanksService]

  object MockBanksService {
    def create(nino: Nino, bank: Bank) = {
      when(mockBanksService.create(eqTo(nino), eqTo(bank))(any()))
    }

    def update(nino: Nino, bank: Bank, id: SourceId) = {
      when(mockBanksService.update(eqTo(nino), eqTo(bank), eqTo(id))(any()))
    }

    def retrieve(nino: Nino, id: SourceId) = {
      when(mockBanksService.retrieve(eqTo(nino), eqTo(id))(any()))
    }

    def retrieveAll(nino: Nino) = {
      when(mockBanksService.retrieveAll(eqTo(nino))(any()))
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBanksService)
  }
}
