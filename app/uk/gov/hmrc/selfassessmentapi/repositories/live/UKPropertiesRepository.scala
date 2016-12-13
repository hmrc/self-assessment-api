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
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONNull, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{UKProperties, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UKPropertiesRepository extends MongoDbConnection {
  private lazy val repository = new UKPropertiesMongoRepository()

  def apply() = repository
}

class UKPropertiesMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[UKProperties, BSONObjectID](
    "ukProperties",
    mongo,
    domainFormat = UKProperties.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with SourceRepository[UKProperty] with AtomicUpdate[UKProperties] with TypedSourceSummaryRepository[UKProperties, BSONObjectID] {

  self =>

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("ukp_nino_taxyear"), unique = false),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending)), name = Some("ukp_nino_taxyear_sourceid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("incomes.summaryId", Ascending)), name = Some("ukp_nino_taxyear_source_incomesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("expenses.summaryId", Ascending)), name = Some("ukp_nino_taxyear_source_expensesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("balancingCharges.summaryId", Ascending)), name = Some("ukp_nino_taxyear_source_balancingchargesid"), unique = true),
    Index(Seq(("nino", Ascending), ("taxYear", Ascending), ("sourceId", Ascending), ("privateUseAdjustment.summaryId", Ascending)), name = Some("ukp_nino_taxyear_source_privateuseadjustmentid"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("ukp_last_modified"), unique = false))


  override def create(nino: Nino, taxYear: TaxYear, ukp: UKProperty): Future[SourceId] = {
    val mongoSe = UKProperties.create(nino, taxYear, ukp)
    insert(mongoSe).map(_ => mongoSe.sourceId)
  }

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[UKProperty]] = {
    for (option <- findMongoObjectById(nino, taxYear, id)) yield option.map(_.toUKProperties)
  }

  override def list(nino: Nino, taxYear: TaxYear): Future[Seq[UKProperty]] = {
    findAll(nino, taxYear).map(_.map(_.toUKProperties))
  }

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    list(nino, taxYear).map(_.map(ukp => JsonItem(ukp.id.get.toString, toJson(ukp))))
  }

  def findAll(nino: Nino, taxYear: TaxYear): Future[Seq[UKProperties]] = {
    find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)
  }

  /*
    We need to perform updates manually as we are using one collection per source and it includes the arrays of summaries. This
    update is however partial so we should only update the fields provided and not override the summary arrays.
   */
  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, ukProperty: UKProperty): Future[Boolean] = {
    val baseModifiers = Seq(
      "$set" -> BSONDocument("rentARoomRelief" -> ukProperty.rentARoomRelief.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull).asInstanceOf[BSONDouble]),
      modifierStatementLastModified
    )

    val allowancesModifiers = ukProperty.allowances.map(allowances =>
      Seq(
        "$set" -> BSONDocument("allowances" -> BSONDocument(Seq(
          "annualInvestmentAllowance" -> allowances.annualInvestmentAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "businessPremisesRenovationAllowance" -> allowances.businessPremisesRenovationAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "otherCapitalAllowance" -> allowances.otherCapitalAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "wearAndTearAllowance" -> allowances.wearAndTearAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))
      )
    ).getOrElse(Seq("$set" -> BSONDocument("allowances" -> BSONNull)))

    val adjustmentsModifiers = ukProperty.adjustments.map(adjustments =>
      Seq(
        "$set" -> BSONDocument("adjustments" -> BSONDocument(Seq(
          "lossBroughtForward" -> adjustments.lossBroughtForward.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
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
      self.createSummary(nino, taxYear, sourceId, UKPropertiesIncomeSummary.toMongoSummary(income))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Income]] =
      self.findSummaryById[Income](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.incomes.find(_.summaryId == id).map(_.toIncome))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: Income): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, UKPropertiesIncomeSummary.toMongoSummary(income, Some(id)), (ukp: UKProperties) => ukp.incomes.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, UKPropertiesIncomeSummary.arrayName, (ukp: UKProperties) => ukp.incomes.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Income]]] =
      self.listSummaries[Income](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.incomes.map(_.toIncome))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(income => JsonItem(income.id.get.toString, toJson(income))))
  }

  object ExpenseRepository extends SummaryRepository[Expense] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, expense: Expense): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, UKPropertiesExpenseSummary.toMongoSummary(expense))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[Expense]] =
      self.findSummaryById[Expense](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.expenses.find(_.summaryId == id).map(_.toExpense))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, expense: Expense): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, UKPropertiesExpenseSummary.toMongoSummary(expense, Some(id)), (ukp: UKProperties) => ukp.expenses.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, UKPropertiesExpenseSummary.arrayName, (ukp: UKProperties) => ukp.expenses.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[Expense]]] =
      self.listSummaries[Expense](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.expenses.map(_.toExpense))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(expense => JsonItem(expense.id.get.toString, toJson(expense))))
  }

  object BalancingChargeRepository extends SummaryRepository[BalancingCharge] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, balancingCharge: BalancingCharge): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, UKPropertiesBalancingChargeSummary.toMongoSummary(balancingCharge))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[BalancingCharge]] =
      self.findSummaryById[BalancingCharge](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.balancingCharges.find(_.summaryId == id).map(_.toBalancingCharge))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, balancingCharge: BalancingCharge): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, UKPropertiesBalancingChargeSummary.toMongoSummary(balancingCharge, Some(id)),
        (ukp: UKProperties) => ukp.balancingCharges.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, UKPropertiesBalancingChargeSummary.arrayName, (ukp: UKProperties) => ukp.balancingCharges.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[BalancingCharge]]] =
      self.listSummaries[BalancingCharge](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.balancingCharges.map(_.toBalancingCharge))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(balancingCharge => JsonItem(balancingCharge.id.get.toString, toJson(balancingCharge))))
  }

  object PrivateUseAdjustmentRepository extends SummaryRepository[PrivateUseAdjustment] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, goodsAndServicesOwnUse: PrivateUseAdjustment): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, UKPropertiesPrivateUseAdjustmentSummary.toMongoSummary(goodsAndServicesOwnUse))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[PrivateUseAdjustment]] =
      self.findSummaryById[PrivateUseAdjustment](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.privateUseAdjustment.find(_.summaryId == id).map(_.toGoodsAndServicesOwnUse))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, goodsAndServicesOwnUse: PrivateUseAdjustment): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, UKPropertiesPrivateUseAdjustmentSummary.toMongoSummary(goodsAndServicesOwnUse, Some(id)),
        (ukp: UKProperties) => ukp.privateUseAdjustment.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, UKPropertiesPrivateUseAdjustmentSummary.arrayName, (ukp: UKProperties) => ukp.privateUseAdjustment.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[PrivateUseAdjustment]]] =
      self.listSummaries[PrivateUseAdjustment](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.privateUseAdjustment.map(_.toGoodsAndServicesOwnUse))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(privateUseAdjustment => JsonItem(privateUseAdjustment.id.get.toString, toJson(privateUseAdjustment))))
  }

  object TaxPaidRepository extends SummaryRepository[TaxPaid] {
    override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, goodsAndServicesOwnUse: TaxPaid): Future[Option[SummaryId]] =
      self.createSummary(nino, taxYear, sourceId, UKPropertiesTaxPaidSummary.toMongoSummary(goodsAndServicesOwnUse))

    override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[TaxPaid]] =
      self.findSummaryById[TaxPaid](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.taxesPaid.find(_.summaryId == id).map(_.toTaxPaid))

    override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, goodsAndServicesOwnUse: TaxPaid): Future[Boolean] =
      self.updateSummary(nino, taxYear, sourceId, UKPropertiesTaxPaidSummary.toMongoSummary(goodsAndServicesOwnUse, Some(id)),
        (ukp: UKProperties) => ukp.taxesPaid.exists(_.summaryId == id))

    override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] =
      self.deleteSummary(nino, taxYear, sourceId, id, UKPropertiesTaxPaidSummary.arrayName, (ukp: UKProperties) => ukp.taxesPaid.exists(_.summaryId == id))

    override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[TaxPaid]]] =
      self.listSummaries[TaxPaid](nino, taxYear, sourceId, (ukp: UKProperties) => ukp.taxesPaid.map(_.toTaxPaid))

    override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
      list(nino, taxYear, sourceId).map(_.getOrElse(Seq()).map(privateUseAdjustment => JsonItem(privateUseAdjustment.id.get.toString, toJson(privateUseAdjustment))))
  }

}