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
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.r2.selfassessmentapi.domain.Dividends

import scala.concurrent.{ExecutionContext, Future}

class DividendsRepository(implicit mongo: () => DB)
  extends ReactiveRepository[Dividends, BSONObjectID](
    "dividends",
    mongo,
    Dividends.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)  {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending)), name = Some("dividends_nino"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("dividends_lastmodified"), unique = false)
  )

  def retrieve(nino: Nino)(implicit ec: ExecutionContext): Future[Option[Dividends]] = find("nino" -> nino.nino).map(_.headOption)

  def create(dividends: Dividends)(implicit ec: ExecutionContext): Future[Boolean] = insert(dividends).map(_.ok)

  def update(nino: Nino, dividends: Dividends)(implicit ec: ExecutionContext): Future[Boolean] = {
    domainFormatImplicit.writes(dividends.copy(lastModifiedDateTime = DateTime.now(DateTimeZone.UTC))) match {
      case d @ JsObject(_) =>
        collection.update(
          BSONDocument("nino" -> nino.nino),
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
      BSONDocument("$lt" -> BSONDateTime(lastModifiedDateTime.getMillis)))

    collection.remove(query).map(_.n)
  }
}

object DividendsRepository extends MongoDbConnection {
  private lazy val repository = new DividendsRepository

  def apply(): DividendsRepository = repository
}
