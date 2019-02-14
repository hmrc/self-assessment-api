/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.repositories

import javax.inject.Inject
import play.modules.reactivemongo.{MongoDbConnection, ReactiveMongoComponent}
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson._
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.r2.selfassessmentapi.domain.MtdRefEntry
import uk.gov.hmrc.r2.selfassessmentapi.models.MtdId
import uk.gov.hmrc.r2.selfassessmentapi.domain.MtdRefEntry

import scala.concurrent.{ExecutionContext, Future}


class MtdReferenceRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit val ec: ExecutionContext) extends ReactiveRepository[MtdRefEntry, BSONObjectID](
  "mtdRef",
  reactiveMongoComponent.mongoConnector.db,
  domainFormat = MtdRefEntry.format,
  idFormat = ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending)), name = Some("user_nino"), unique = true)
  )

  def store(nino: Nino, mtdId: MtdId)(implicit ec: ExecutionContext): Future[Boolean] =
    insert(MtdRefEntry(nino.nino, mtdId.mtdId)).map { res =>
      if (!res.writeErrors.isEmpty) logger.warn(s"Database error occurred. Errors: [${res.writeErrors}] Code: [${res.code}]")
      res.ok && res.n > 0
    } recoverWith {
      case e: DatabaseException =>
        logger.warn(s"Exception in database occurred. Exception: [$e]")
        Future.successful(false)
    }

  def retrieve(nino: Nino)(implicit ec: ExecutionContext): Future[Option[MtdId]] =
    find("nino" -> nino.nino)
      .map(_.headOption.map(entry => MtdId(entry.mtdRef)))
}

//object MtdReferenceRepository extends MongoDbConnection {
//  private lazy val repository = new MtdReferenceRepository()
//
//  def apply(): MtdReferenceRepository = repository
//}
