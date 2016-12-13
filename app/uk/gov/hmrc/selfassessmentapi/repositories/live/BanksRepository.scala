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

import play.api.libs.json.Json._
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.{Interest, Bank => ApiBank}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Bank, BankInterestSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository, SummaryRepository, TypedSourceSummaryRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BanksRepository extends MongoDbConnection {
  private lazy val repository = new BanksMongoRepository()

  def apply() = repository
}

class BanksMongoRepository(implicit mongo: () => DB) extends ReactiveRepository[Bank, BSONObjectID](
  "banks",
  mongo,
  domainFormat = Bank.mongoFormats,
  idFormat = ReactiveMongoFormats.objectIdFormats)
  with SourceRepository[ApiBank] with TypedSourceSummaryRepository[Bank, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("banks_nino_taxyear"), unique = false),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("banks_nino_taxyear_sourceid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("interests.summaryId", Ascending)), name = Some("banks_nino_taxyear_source_interestsid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("banks_last_modified"), unique = false)
  )

  override def create(nino: Nino, taxYear: TaxYear, bank: ApiBank): Future[SourceId] = {
    val mongoBank = Bank.create(nino, taxYear)
    insert(mongoBank).map(_ => mongoBank.sourceId)
  }

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId) = {
    for(option <- findMongoObjectById(nino, taxYear, id)) yield option.map(_.toBank)
  }

  override def list(nino: Nino, taxYear: TaxYear) = {
    for (list <- find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)) yield list.map(_.toBank)
  }

  def findAll(nino: Nino, taxYear: TaxYear): Future[Seq[Bank]] = {
    find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)
  }

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear) =
    list(nino, taxYear).map(_.map(sav => JsonItem(sav.id.get.toString, toJson(sav))))

  /*
  We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
  update is however partial so we should only update the fields provided and not override the summary arrays.
 */
  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, bank: ApiBank) = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
        BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object InterestRepository extends SummaryRepository[Interest] {

    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, interest: Interest): Future[Option[SummaryId]] = {
      self.createSummary(nino, taxYear, sourceId, BankInterestSummary.toMongoSummary(interest))
    }

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Interest]] = {
      self.findSummaryById(nino, taxYear, sourceId, _.interests.find(_.summaryId == id).map(_.toBankInterest))
    }

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, interest: Interest): Future[Boolean] = {
      self.updateSummary(nino, taxYear, sourceId, BankInterestSummary.toMongoSummary(interest, Some(id)), _.interests.exists(_.summaryId == id))
    }

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] = {
      self.deleteSummary(nino, taxYear, sourceId, id, BankInterestSummary.arrayName, _.interests.exists(_.summaryId == id))
    }

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Interest]]] = {
      self.listSummaries(nino, taxYear, sourceId, _.interests.map(_.toBankInterest))
    }

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] = {
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
    }
  }
}
