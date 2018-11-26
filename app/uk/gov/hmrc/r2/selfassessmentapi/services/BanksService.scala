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

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi._
import uk.gov.hmrc.r2.selfassessmentapi.repositories.BanksRepository
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceId
import uk.gov.hmrc.r2.selfassessmentapi.models.banks.Bank

import scala.concurrent.{ExecutionContext, Future}

object BanksService extends BanksService {
  override val mongoRepository = BanksRepository()
}

trait BanksService {

  val mongoRepository: BanksRepository

  def create(nino: Nino, bank: Bank)(implicit ec: ExecutionContext): Future[Option[SourceId]] = {
    val id = BSONObjectID.generate
    val newBank =
      domain.Bank(id, id.stringify, nino, DateTime.now(DateTimeZone.UTC),
        bank.accountName, Map.empty)
    mongoRepository.create(newBank).map {
      case true => Some(newBank.sourceId)
      case false => None
    }
  }

  def update(nino: Nino, bank: Bank, id: SourceId)(implicit ec: ExecutionContext): Future[Boolean] = {
    mongoRepository.retrieve(id, nino).flatMap {
      case Some(oldBank) =>
        mongoRepository.update(id, nino, oldBank.copy(accountName = bank.accountName))
      case None => Future.successful(false)
    }
  }

  def retrieve(nino: Nino, id: SourceId)(implicit ec: ExecutionContext): Future[Option[Bank]] =
    mongoRepository.retrieve(id, nino).map(_.map(_.toModel(true)))

  def retrieveAll(nino: Nino)(implicit ec: ExecutionContext): Future[Seq[Bank]] =
    mongoRepository.retrieveAll(nino).map(_.map(_.toModel()))

}
