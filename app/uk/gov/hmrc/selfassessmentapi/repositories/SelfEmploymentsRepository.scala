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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTimeZone, LocalDate}
import play.api.libs.json.JsObject
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceId
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.services.NewSourceRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentsRepository(implicit mongo: () => DB)
  extends NewSourceRepository[SourceId, SelfEmployment]("selfEmployments", SelfEmployment.mongoFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending)), name = Some("se_nino"), unique = false),
    Index(Seq(("nino", Ascending), ("sourceId", Ascending)), name = Some("se_nino_sourceId"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("se_lastmodified"), unique = false))

  def retrieve(id: SourceId, nino: Nino): Future[Option[SelfEmployment]] = {
    find("nino" -> nino.nino, "sourceId" -> id).map(_.headOption)
  }

  def retrieveAll(nino: Nino): Future[Seq[SelfEmployment]] = {
    find("nino" -> nino.nino)
  }

  /*
   * Inserts a new SelfEmployment document.
   */
  def create(selfEmployment: SelfEmployment): Future[Boolean] = {
    insert(selfEmployment).map { res =>
      if (res.hasErrors) logger.error(s"Database error occurred. Error: ${res.errmsg} Code: ${res.code}")
      res.ok
    }
  }

  /*
   * Replaces the whole SelfEmployment document with the `newSelfEmployment` document.
   *
   * This method should be used whenever making a change to a SelfEmployment document.
   * Hand-written persistence code is discouraged.
   */
  def update(id: SourceId, nino: Nino, newSelfEmployment: SelfEmployment): Future[Boolean] = {
    domainFormatImplicit.writes(newSelfEmployment.copy(lastModifiedDateTime = LocalDate.now(DateTimeZone.UTC))) match {
      case d @ JsObject(_) => collection.update(
        BSONDocument("nino" -> nino.nino, "sourceId" -> id),
        d
      ).map { res =>
        if (res.hasErrors) logger.error(s"Database error occurred. Error: ${res.errmsg} Code: ${res.code}")
        res.ok && res.nModified > 0
      }
      case _ => Future.successful(false)
    }
  }

}

object SelfEmploymentsRepository extends MongoDbConnection {
  private lazy val repository = new SelfEmploymentsRepository()

  def apply() = repository
}
