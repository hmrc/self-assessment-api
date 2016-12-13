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

import org.joda.time.DateTimeZone
import play.api.libs.json.Json.toJson
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDouble, BSONNull, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{SelfEmployment, SelfEmploymentExpenseSummary, SelfEmploymentIncomeSummary}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentRepository extends MongoDbConnection {
  private lazy val repository = new SelfEmploymentMongoRepository()

  def apply() = repository
}

class SelfEmploymentMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SelfEmployment, BSONObjectID](
    "selfEmployments",
    mongo,
    domainFormat = SelfEmployment.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[selfemployment.SelfEmployment] with AtomicUpdate[SelfEmployment] with TypedSourceSummaryRepository[SelfEmployment, BSONObjectID]{

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("se_nino_taxyear"), unique = false),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("se_nino_taxyear_sourceid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("se_nino_taxyear_source_incomesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("expenses.summaryId", Ascending)), name = Some("se_nino_taxyear_source_expensesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("balancingCharges.summaryId", Ascending)), name = Some("se_nino_taxyear_source_balancingchargesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("goodsAndServicesOwnUse.summaryId", Ascending)), name = Some("se_nino_taxyear_source_goodsandservicesownuseid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("se_last_modified"), unique = false))


  override def create(nino: Nino, taxYear: TaxYear, se: selfemployment.SelfEmployment): Future[SourceId] = {
    val mongoSe = SelfEmployment.create(nino, taxYear, se)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[selfemployment.SelfEmployment]] = {
    for(option <- findMongoObjectById(nino, taxYear, id)) yield option.map(_.toSelfEmployment)
  }

  override def list(nino: Nino, taxYear: TaxYear): Future[Seq[selfemployment.SelfEmployment]] = {
    findAll(nino, taxYear).map(_.map(_.toSelfEmployment))
  }

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    list(nino, taxYear).map(_.map(se => JsonItem(se.id.get.toString, toJson(se))))
  }

  def findAll(nino: Nino, taxYear: TaxYear): Future[Seq[SelfEmployment]] = {
    find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)
  }

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, selfEmployment: api.selfemployment.SelfEmployment): Future[Boolean] = {
    val baseModifiers = Seq(
      "$set" -> BSONDocument("commencementDate" -> BSONDateTime(selfEmployment.commencementDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis)),
      modifierStatementLastModified
    )

    val allowancesModifiers = selfEmployment.allowances.map(allowances =>
      Seq(
        "$set" -> BSONDocument("allowances" -> BSONDocument(Seq(
          "annualInvestmentAllowance" -> allowances.annualInvestmentAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "capitalAllowanceMainPool" -> allowances.capitalAllowanceMainPool.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "capitalAllowanceSpecialRatePool" -> allowances.capitalAllowanceSpecialRatePool.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "businessPremisesRenovationAllowance" -> allowances.businessPremisesRenovationAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "enhancedCapitalAllowance" -> allowances.enhancedCapitalAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "allowanceOnSales" -> allowances.allowanceOnSales.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("allowances" -> BSONNull)))

    val adjustmentsModifiers = selfEmployment.adjustments.map(adjustments =>
      Seq(
        "$set" -> BSONDocument("adjustments" -> BSONDocument(Seq(
          "accountingAdjustment" -> adjustments.accountingAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "averagingAdjustment" -> adjustments.averagingAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "basisAdjustment" -> adjustments.basisAdjustment.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "includedNonTaxableProfits" -> adjustments.includedNonTaxableProfits.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "lossBroughtForward" -> adjustments.lossBroughtForward.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "outstandingBusinessIncome" -> adjustments.outstandingBusinessIncome.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "overlapReliefUsed" -> adjustments.overlapReliefUsed.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("adjustments" -> BSONNull)))

    val modifiers = BSONDocument(baseModifiers ++ allowancesModifiers ++ adjustmentsModifiers)

    for {
      result <- atomicUpdate(
        BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(id)),
        modifiers
      )
    } yield result.nonEmpty
  }

  object IncomeRepository extends SummaryRepository[Income] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, income: Income): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, SelfEmploymentIncomeSummary.toMongoSummary(income))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](nino, taxYear, sourceId, (se: SelfEmployment) => se.incomes.find(_.summaryId == id).map(_.toIncome))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, SelfEmploymentIncomeSummary.toMongoSummary(income, Some(id)), (se: SelfEmployment) => se.incomes.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, SelfEmploymentIncomeSummary.arrayName, (se: SelfEmployment) => se.incomes.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](nino, taxYear, sourceId, (se: SelfEmployment) => se.incomes.map(_.toIncome))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear,sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
  }

  object ExpenseRepository extends SummaryRepository[Expense] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, expense: Expense): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, SelfEmploymentExpenseSummary.toMongoSummary(expense))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Expense]] =
      self.findSummaryById[Expense](nino, taxYear, sourceId, (se: SelfEmployment) => se.expenses.find(_.summaryId == id).map(_.toExpense))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, expense: Expense): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, SelfEmploymentExpenseSummary.toMongoSummary(expense, Some(id)), (se: SelfEmployment) => se.expenses.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, SelfEmploymentExpenseSummary.arrayName, (se: SelfEmployment) => se.expenses.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Expense]]] =
      self.listSummaries[Expense](nino, taxYear, sourceId, (se: SelfEmployment) => se.expenses.map(_.toExpense))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear,sourceId).map(_.getOrElse(Seq()).map(expense => JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BalancingChargeRepository extends SummaryRepository[BalancingCharge] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, balancingCharge: BalancingCharge): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, SelfEmploymentBalancingChargeSummary.toMongoSummary(balancingCharge))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[BalancingCharge]] =
      self.findSummaryById[BalancingCharge](nino, taxYear, sourceId, (se: SelfEmployment) => se.balancingCharges.find(_.summaryId == id).map(_.toBalancingCharge))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, balancingCharge: BalancingCharge): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, SelfEmploymentBalancingChargeSummary.toMongoSummary(balancingCharge, Some(id)),
        (se: SelfEmployment) => se.balancingCharges.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, SelfEmploymentBalancingChargeSummary.arrayName, (se: SelfEmployment) => se.balancingCharges.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[BalancingCharge]]] =
      self.listSummaries[BalancingCharge](nino, taxYear, sourceId, (se: SelfEmployment) => se.balancingCharges.map(_.toBalancingCharge))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear,sourceId).map(_.getOrElse(Seq()).map(balancingCharge => JsonItem(balancingCharge.id.get.toString, toJson(balancingCharge))))
  }

  object GoodsAndServicesOwnUseRepository extends SummaryRepository[GoodsAndServicesOwnUse] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, goodsAndServicesOwnUse: GoodsAndServicesOwnUse): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, SelfEmploymentGoodsAndServicesOwnUseSummary.toMongoSummary(goodsAndServicesOwnUse))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[GoodsAndServicesOwnUse]] =
      self.findSummaryById[GoodsAndServicesOwnUse](nino, taxYear, sourceId, (se: SelfEmployment) => se.goodsAndServicesOwnUse.find(_.summaryId == id).map(_.toGoodsAndServicesOwnUse))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, goodsAndServicesOwnUse: GoodsAndServicesOwnUse): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, SelfEmploymentGoodsAndServicesOwnUseSummary.toMongoSummary(goodsAndServicesOwnUse, Some(id)),
        (se: SelfEmployment) => se.goodsAndServicesOwnUse.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, SelfEmploymentGoodsAndServicesOwnUseSummary.arrayName, (se: SelfEmployment) => se.goodsAndServicesOwnUse.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[GoodsAndServicesOwnUse]]] =
      self.listSummaries[GoodsAndServicesOwnUse](nino, taxYear, sourceId, (se: SelfEmployment) => se.goodsAndServicesOwnUse.map(_.toGoodsAndServicesOwnUse))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear,sourceId).map(_.getOrElse(Seq()).map(goodsAndServicesOwnUse => JsonItem(goodsAndServicesOwnUse.id.get.toString, toJson(goodsAndServicesOwnUse))))
  }


}
