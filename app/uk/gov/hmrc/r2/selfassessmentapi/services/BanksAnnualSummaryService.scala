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

package uk.gov.hmrc.r2.selfassessmentapi.services

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.repositories.BanksRepository
import uk.gov.hmrc.r2.selfassessmentapi.models.{SourceId, TaxYear}
import uk.gov.hmrc.r2.selfassessmentapi.models.banks.BankAnnualSummary

import scala.concurrent.{ExecutionContext, Future}

trait BanksAnnualSummaryService {
  val repository: BanksRepository

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, newBankSummary: BankAnnualSummary)(implicit ec: ExecutionContext): Future[Boolean] = {
    repository.retrieve(id, nino).flatMap {
      case Some(bank) =>
        repository.update(id, nino, bank.copy(annualSummaries = bank.annualSummaries.updated(taxYear, newBankSummary)))
      case None => Future.successful(false)
    }
  }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear)(implicit ec: ExecutionContext): Future[Option[BankAnnualSummary]] = {
    repository.retrieve(id, nino).map {
      case Some(resource) => Some(resource.annualSummary(taxYear))
      case None => None
    }
  }
}

object BanksAnnualSummaryService extends BanksAnnualSummaryService {
  override val repository: BanksRepository = BanksRepository()
}
