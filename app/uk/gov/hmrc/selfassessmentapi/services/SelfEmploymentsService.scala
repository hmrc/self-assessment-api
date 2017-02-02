/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.repositories.SelfEmploymentsRepository
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment._
import uk.gov.hmrc.selfassessmentapi.resources.models.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.errors.BusinessException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SelfEmploymentsMongoService {

  val mongoRepository: SelfEmploymentsRepository

  def create(nino: Nino, selfEmployment: SelfEmployment): Future[Option[SourceId]] = {
    val id = BSONObjectID.generate
    val newSelfEmployment =
      domain.SelfEmployment(id, id.stringify, nino, DateTime.now(DateTimeZone.UTC),
        selfEmployment.accountingPeriod, selfEmployment.accountingType, selfEmployment.commencementDate,
        selfEmployment.tradingName, selfEmployment.businessDescription, selfEmployment.businessAddressLineOne,
        selfEmployment.businessAddressLineTwo, selfEmployment.businessAddressLineThree,
        selfEmployment.businessAddressLineFour, selfEmployment.businessPostcode)

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

  def update(nino: Nino, selfEmployment: SelfEmployment, id: SourceId): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(oldSelfEmployment) =>
        mongoRepository.update(id, nino, oldSelfEmployment.copy(
          accountingPeriod = selfEmployment.accountingPeriod,
          accountingType = selfEmployment.accountingType,
          commencementDate = selfEmployment.commencementDate))
      case None => Future.successful(false)
    }
  }

  def retrieve(nino: Nino, id: SourceId): Future[Option[SelfEmployment]] =
    mongoRepository.retrieve(id, nino).map(_.map(_.toModel(true)))

  def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]] =
    mongoRepository.retrieveAll(nino).map(_.map(_.toModel()))

}

object SelfEmploymentsService extends SelfEmploymentsMongoService {
  override val mongoRepository = SelfEmploymentsRepository()
}
