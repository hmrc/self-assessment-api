/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models.{selfemployment, _}
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{SelfEmploymentAnnualSummary, SelfEmploymentPeriod, SelfEmploymentPeriodicData}

case class SelfEmployment(id: BSONObjectID,
                          sourceId: String,
                          nino: Nino,
                          lastModifiedDateTime: DateTime,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          tradingName: String,
                          businessDescription: String,
                          businessAddressLineOne: String,
                          businessAddressLineTwo: Option[String],
                          businessAddressLineThree: Option[String],
                          businessAddressLineFour: Option[String],
                          businessPostcode: String,
                          annualSummaries: Map[TaxYear, SelfEmploymentAnnualSummary] = Map.empty,
                          periods: Map[PeriodId, SelfEmploymentPeriod] = Map.empty)
    extends PeriodValidator[SelfEmploymentPeriod]
    with LastModifiedDateTime {

  def toModel(elideID: Boolean = false): selfemployment.SelfEmployment = {
    val id = if (elideID) None else Some(sourceId)
    selfemployment.SelfEmployment(
      id, accountingPeriod, accountingType, commencementDate,
      tradingName, businessDescription, businessAddressLineOne, businessAddressLineTwo,
      businessAddressLineThree, businessAddressLineFour, businessPostcode)
  }

  def validatePeriod(period: SelfEmploymentPeriod): Option[Error] = validatePeriod(period, accountingPeriod)

  def annualSummary(key: TaxYear): SelfEmploymentAnnualSummary =
    annualSummaries.getOrElse(key, SelfEmploymentAnnualSummary(None, None))

  def periodExists(periodId: PeriodId): Boolean = period(periodId).nonEmpty

  def period(periodId: PeriodId): Option[SelfEmploymentPeriod] = periods.get(periodId)

  def setPeriodsTo(periodId: PeriodId, period: SelfEmploymentPeriod): SelfEmployment =
    this.copy(periods = periods.updated(periodId, period))

  def update(periodId: PeriodId, periodicData: SelfEmploymentPeriodicData): SelfEmployment = {
    periods.find(period => period._1.equals(periodId)).map { period =>
      setPeriodsTo(periodId, period._2.copy(data = periodicData))
    }.get
  }
}

object SelfEmployment {
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.annualSummaryMapFormat

    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[SelfEmployment], Json.writes[SelfEmployment])
  })
}
