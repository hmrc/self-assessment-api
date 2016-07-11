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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain._

case class MongoLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          createdDateTime: DateTime,
                          profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                          interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                          totalIncomeReceived: Option[BigDecimal] = None,
                          totalTaxableIncome: Option[BigDecimal] = None,
                          personalAllowance: Option[BigDecimal] = None,
                          deductions: Option[Deductions] = None,
                          totalIncomeOnWhichTaxIsDue: Option[BigDecimal] = None) {

  def toLiability =
    Liability(
      id = Some(liabilityId),
      income = IncomeSummary(
        incomes = IncomeFromSources(selfEmployment = profitFromSelfEmployments.map(_.toIncome), interestFromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties, employment = Nil),
        deductions = deductions,
        totalIncomeReceived = totalIncomeReceived.getOrElse(0),
        personalAllowance = personalAllowance.getOrElse(0),
        totalTaxableIncome = totalTaxableIncome.getOrElse(0),
        totalIncomeOnWhichTaxIsDue = totalIncomeOnWhichTaxIsDue.getOrElse(0)
      ),
      incomeTax = CalculatedAmount(calculations = Nil, total = 0),
      credits = Nil,
      class4Nic = CalculatedAmount(calculations = Nil, total = 0),
      totalTaxDue = 0
    )
}

case class SelfEmploymentIncome(sourceId: SourceId, taxableProfit: BigDecimal, profit: BigDecimal, lossBroughtForward: BigDecimal) {

  def toIncome = Income(sourceId, taxableProfit, profit)
}

object MongoLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
  implicit val incomeFormats: Format[SelfEmploymentIncome] = Json.format[SelfEmploymentIncome]
  implicit val mongoFormats = Json.format[MongoLiability]

  def create(saUtr: SaUtr, taxYear: TaxYear): MongoLiability = {
    val id = BSONObjectID.generate
    MongoLiability(
      id = id,
      liabilityId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      createdDateTime = DateTime.now(DateTimeZone.UTC)
    )
  }
}

