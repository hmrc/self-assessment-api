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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.repositories.domain.LiabilityResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LiabilityRepository extends MongoDbConnection {
  private lazy val repository = new LiabilityMongoRepository()
  def apply() = repository
}

class LiabilityMongoRepository(implicit mongo: () => DB)
    extends ReactiveRepository[LiabilityResult, BSONObjectID]("liabilities",
                                                              mongo,
                                                              domainFormat = LiabilityResult.liabilityResultFormat,
                                                              idFormat = ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] =
    Seq(
        Index(Seq(("data.nino", Ascending), ("data.taxYear", Ascending)),
              name = Some("liabilities_nino_taxyear"),
              unique = true))

  def save[T <: LiabilityResult](liabilityResult: T): Future[T] = {
    val selector = BSONDocument("data.nino" -> liabilityResult.nino.value, "data.taxYear" -> liabilityResult.taxYear.value)
    collection.update(selector, liabilityResult, upsert = true).map(_ => liabilityResult)
  }

  def findBy(nino: Nino, taxYear: TaxYear): Future[Option[LiabilityResult]] = {
    find("data.nino" -> nino.value, "data.taxYear" -> taxYear.value).map(_.headOption)
  }
}
