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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.PeriodId
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.resources.models.{selfemployment, _}
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{SelfEmploymentAnnualSummary, SelfEmploymentPeriod, SelfEmploymentPeriodicData}

case class SelfEmployment(id: BSONObjectID,
                          sourceId: String,
                          nino: Nino,
                          lastModifiedDateTime: LocalDate,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          annualSummaries: Map[TaxYear, SelfEmploymentAnnualSummary] = Map.empty,
                          periods: Map[PeriodId, SelfEmploymentPeriod] = Map.empty)
  extends PeriodContainer[SelfEmploymentPeriod, SelfEmployment, SelfEmploymentPeriodicData]
    with AnnualSummaryContainer[SelfEmploymentAnnualSummary]
    with LastModifiedDateTime {

  override def containsMisalignedPeriod(period: SelfEmploymentPeriod): Boolean = {
    val alignedWithEnd = period.to.isBefore(accountingPeriod.end) || period.to.isEqual(accountingPeriod.end)

    if (periods.isEmpty) !(period.from.isEqual(accountingPeriod.start) && alignedWithEnd)
    else !alignedWithEnd
  }

  def toModel(elideID: Boolean = false): selfemployment.SelfEmployment = {
    val id = if (elideID) None else Some(sourceId)
    selfemployment.SelfEmployment(id, accountingPeriod, accountingType, commencementDate)
  }

  override def setPeriodsTo(periodId: PeriodId, period: SelfEmploymentPeriod): SelfEmployment =
    this.copy(periods = periods.updated(periodId, period))

  override def update(periodId: PeriodId, periodicData: SelfEmploymentPeriodicData): SelfEmployment = {
    this.periods.find(period => period._1.equals(periodId)).map { period =>
      setPeriodsTo(periodId, period._2.copy(data = periodicData))
    }.get
  }
}

object SelfEmployment {
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.annualSummaryMapFormat

    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[SelfEmployment], Json.writes[SelfEmployment])
  })
}
