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

package uk.gov.hmrc.r2.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.JsObject
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.r2.selfassessmentapi.domain.Bank
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceId
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

class BanksRepository(implicit mongo: () => DB) extends ReactiveRepository[Bank, BSONObjectID](
  "banks",
  mongo,
  Bank.mongoFormats,
  idFormat = ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending)), name = Some("ba_nino"), unique = false),
    Index(Seq(("nino", Ascending), ("sourceId", Ascending)), name = Some("ba_nino_sourceId"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("ba_lastmodified"), unique = false))

  def retrieve(id: SourceId, nino: Nino)(implicit ec: ExecutionContext): Future[Option[Bank]] = {
    find("nino" -> nino.nino, "sourceId" -> id).map(_.headOption)
  }

  def retrieveAll(nino: Nino)(implicit ec: ExecutionContext): Future[Seq[Bank]] = {
    find("nino" -> nino.nino)
  }

  def create(bank: Bank)(implicit ec: ExecutionContext): Future[Boolean] = {
    insert(bank).map { res =>
      if (!res.writeErrors.isEmpty) logger.warn(s"Database error occurred. Errors: ${res.writeErrors} Code: ${res.code}")
      res.ok
    }
  }

  def update(id: SourceId, nino: Nino, newBank: Bank)(implicit ec: ExecutionContext): Future[Boolean] = {
    domainFormatImplicit.writes(newBank.copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC))) match {
      case d @ JsObject(_) => collection.update(
        BSONDocument("nino" -> nino.nino, "sourceId" -> id),
        d
      ).map { res =>
        if (!res.writeErrors.isEmpty) logger.warn(s"Database error occurred. Error: ${res.errmsg} Code: ${res.code}")
        res.ok && res.nModified > 0
      }
      case _ => Future.successful(false)
    }
  }

  def deleteAllBeforeDate(lastModifiedDateTime: DateTime)(implicit ec: ExecutionContext): Future[Int] = {
    val query = BSONDocument("lastModifiedDateTime" ->
      BSONDocument("$lte" -> BSONDateTime(lastModifiedDateTime.getMillis)))

    collection.remove(query).map(_.n)
  }
}

object BanksRepository extends MongoDbConnection {
  private lazy val repository = new BanksRepository()
  def apply(): BanksRepository = repository
}
