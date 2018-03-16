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

package uk.gov.hmrc.selfassessmentapi.services

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.domain.Dividends
import uk.gov.hmrc.selfassessmentapi.repositories.DividendsRepository
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.TaxYear

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait DividendsAnnualSummaryService {
  val repository: DividendsRepository

  def updateAnnualSummary(nino: Nino, taxYear: TaxYear, newDividends: models.dividends.Dividends): Future[Boolean] = {
    repository.retrieve(nino).flatMap {
      case Some(resource) => repository.update(nino, resource.copy(dividends = resource.dividends.updated(taxYear, newDividends)))
      case None => repository.create(Dividends(BSONObjectID.generate, nino, Map(taxYear -> newDividends)))
    }
  }

  def retrieveAnnualSummary(nino: Nino, taxYear: TaxYear): Future[Option[models.dividends.Dividends]] = {
    repository.retrieve(nino).map {
      case Some(resource) => resource.dividends.get(taxYear)
      case None => Some(models.dividends.Dividends(None))
    }
  }
}

object DividendsAnnualSummaryService extends DividendsAnnualSummaryService {
  override val repository: DividendsRepository = DividendsRepository()
}
