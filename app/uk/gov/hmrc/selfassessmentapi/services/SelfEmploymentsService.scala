/*
 * Copyright 2016 HM Revenue & Customs
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

import org.joda.time.{DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment._
import uk.gov.hmrc.selfassessmentapi.resources.models.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.errors.BusinessException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentsService extends PeriodService[SourceId, SelfEmploymentPeriod, domain.SelfEmployment, SelfEmploymentPeriodicData]
  with AnnualSummaryService[SelfEmploymentAnnualSummary, domain.SelfEmployment] {

  def create(nino: Nino, selfEmployment: SelfEmployment): Future[Option[SourceId]]
  def update(nino: Nino, selfEmployment: SelfEmployment, id: SourceId): Future[Boolean]
  def retrieve(nino: Nino, id: SourceId): Future[Option[SelfEmployment]]
  def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]]
  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, summary: SelfEmploymentAnnualSummary): Future[Boolean]
  def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[SelfEmploymentAnnualSummary]]
}

object SelfEmploymentsService {
  def apply() = new SelfEmploymentsMongoService(SelfEmploymentsRepository())
}

class SelfEmploymentsMongoService(mongoRepository: SelfEmploymentsRepository) extends SelfEmploymentsService {

  override val periodRepository = mongoRepository
  override val annualSummaryRepository = mongoRepository

  override def create(nino: Nino, selfEmployment: SelfEmployment): Future[Option[SourceId]] = {
    val id = BSONObjectID.generate
    val newSelfEmployment =
      domain.SelfEmployment(id, id.stringify, nino, LocalDate.now(DateTimeZone.UTC),
        selfEmployment.accountingPeriod, selfEmployment.accountingType, selfEmployment.commencementDate)

    mongoRepository.retrieveAll(nino).flatMap { selfEmployments =>
      selfEmployments.size match {
        case count if count == 1 => throw BusinessException("TOO_MANY_SOURCES", "The maximum number of Self-Employment incomes sources is 1")
        case _ =>
          mongoRepository.create(newSelfEmployment).map {
            case true => Some(newSelfEmployment.sourceId)
            case false => None
          }
      }
    }
  }

  override def update(nino: Nino, selfEmployment: SelfEmployment, id: SourceId): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(oldSelfEmployment) =>
        mongoRepository.update(id, nino, oldSelfEmployment.copy(
          accountingPeriod = selfEmployment.accountingPeriod,
          accountingType = selfEmployment.accountingType,
          commencementDate = selfEmployment.commencementDate))
      case None => Future.successful(false)
    }
  }

  override def retrieve(nino: Nino, id: SourceId): Future[Option[SelfEmployment]] =
    mongoRepository.retrieve(id, nino).map(_.map(_.toModel(true)))

  override def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]] =
    mongoRepository.retrieveAll(nino).map(_.map(_.toModel()))


  override def retrieveAnnualSummary(id: SourceId, taxYear: TaxYear, nino: Nino): Future[Option[SelfEmploymentAnnualSummary]] = {
    annualSummaryRepository.retrieve(id, nino).map {
      case Some(resource) => resource.annualSummary(taxYear).orElse(Some(SelfEmploymentAnnualSummary(None, None)))
      case None => None
    }
  }

  override def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, summary: SelfEmploymentAnnualSummary): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(selfEmployment) =>
        mongoRepository.update(id, nino, selfEmployment.copy(annualSummaries = selfEmployment.annualSummaries.updated(taxYear, summary)))
      case None => Future.successful(false)
    }
  }

}
