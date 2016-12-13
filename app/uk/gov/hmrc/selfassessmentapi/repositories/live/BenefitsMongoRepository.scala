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

import play.api.libs.json.Json.toJson
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.Income
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



object BenefitsRepository extends MongoDbConnection {
  private lazy val repository = new BenefitsMongoRepository()
  def apply() = repository
}

class BenefitsMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[Benefits, BSONObjectID](
    "benefits",
    mongo,
    domainFormat = Benefits.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[benefit.Benefit] with AtomicUpdate[Benefits] with TypedSourceSummaryRepository[Benefits, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("ui_nino_taxyear"), unique = false),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("ui_nino_taxyear_sourceid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("ui_nino_taxyear_source_benefitsid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("ui_last_modified"), unique = false))


  override def create(nino: Nino, taxYear: TaxYear, ui: benefit.Benefit): Future[SourceId] = {
    val mongoSe = Benefits.create(nino, taxYear, ui)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[benefit.Benefit]] = {
    for(option <- findMongoObjectById(nino, taxYear, id)) yield option.map(_.toUnearnedIncome)
  }

  override def list(nino: Nino, taxYear: TaxYear): Future[Seq[benefit.Benefit]] = {
    for (list <- find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)) yield list.map(_.toUnearnedIncome)
  }

  def findAll(nino: Nino, taxYear: TaxYear): Future[Seq[Benefits]] = {
    find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)
  }


  override def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]] =
    list(nino, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, benef: benefit.Benefit): Future[Boolean] = {
    val modifiers = BSONDocument(Seq(modifierStatementLastModified))
    for {
      result <- atomicUpdate(
        BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object BenefitRepository extends SummaryRepository[Income] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, benefit: Income): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, BenefitIncomeSummary.toMongoSummary(benefit))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](nino, taxYear, sourceId, (se: Benefits) => se.incomes.find(_.summaryId == id).map(_.toBenefit))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, benefit: Income): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, BenefitIncomeSummary.toMongoSummary(benefit, Some(id)), (se: Benefits) => se.incomes.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, BenefitIncomeSummary.arrayName, (se: Benefits) => se.incomes.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](nino, taxYear, sourceId, (se: Benefits) => se.incomes.map(_.toBenefit))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear,sourceId).map(_.getOrElse(Seq()).map(benefit => JsonItem(benefit.id.get.toString, toJson(benefit))))
  }

}
