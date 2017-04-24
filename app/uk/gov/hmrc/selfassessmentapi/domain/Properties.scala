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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, TaxYear, _}

case class Properties(id: BSONObjectID,
                      nino: Nino,
                      lastModifiedDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                      accountingPeriod: AccountingPeriod =
                        AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
                      fhlBucket: FHLPropertiesBucket = FHLPropertiesBucket(Map.empty, Map.empty),
                      otherBucket: OtherPropertiesBucket = OtherPropertiesBucket(Map.empty, Map.empty))
    extends LastModifiedDateTime {

  def toModel = properties.Properties()

  def annualSummary(propertyType: PropertyType, key: TaxYear): PropertiesAnnualSummary = propertyType match {
    case PropertyType.OTHER => otherBucket.annualSummaries.getOrElse(key, new OtherPropertiesAnnualSummary(None, None))
    case PropertyType.FHL => fhlBucket.annualSummaries.getOrElse(key, new FHLPropertiesAnnualSummary(None, None))
  }
}

object Properties {
  implicit val mongoFormats: Format[Properties] = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[Properties], Json.writes[Properties])
  })
}

trait PropertyPeriodOps[P <: Period, F <: Financials] {
  def periods(properties: Properties): Map[PeriodId, P]

  def validatePeriod(period: P, properties: Properties): Either[Errors.Error, Properties]

  def periodExists(periodId: PeriodId, properties: Properties): Boolean

  def period(periodId: PeriodId, properties: Properties): Option[P]

  def setPeriodsTo(periodId: PeriodId, period: P, properties: Properties): Properties

  def update(periodId: PeriodId, financials: F, properties: Properties): Properties
}

object PropertyPeriodOps {
  implicit object OtherPeriodOps extends PropertyPeriodOps[Other.Properties, Other.Financials] {

    override def periods(properties: Properties): Map[PeriodId, Other.Properties] = properties.otherBucket.periods

    override def validatePeriod(period: Other.Properties, properties: Properties): Either[Errors.Error, Properties] = {
      val errors = properties.otherBucket.validatePeriod(period, properties.accountingPeriod)
      errors.map(Left(_)).getOrElse(Right(properties))
    }

    override def periodExists(periodId: PeriodId, properties: Properties): Boolean =
      period(periodId, properties).isDefined

    override def period(periodId: PeriodId, properties: Properties): Option[Other.Properties] =
      properties.otherBucket.periods.get(periodId)

    override def setPeriodsTo(periodId: PeriodId, period: Other.Properties, properties: Properties): Properties =
      properties.copy(
        otherBucket = properties.otherBucket.copy(periods = properties.otherBucket.periods.updated(periodId, period)))

    override def update(periodId: PeriodId, financials: Other.Financials, properties: Properties): Properties = {
      val periodOpt = properties.otherBucket.periods.find(period => period._1.equals(periodId))

      periodOpt.map { period =>
        setPeriodsTo(periodId, period._2.copy(financials = financials), properties)
      }.get
    }
  }

  implicit object FHLPeriodOps extends PropertyPeriodOps[FHL.Properties, FHL.Financials] {

    override def periods(properties: Properties): Map[PeriodId, FHL.Properties] = properties.fhlBucket.periods

    override def validatePeriod(period: FHL.Properties, properties: Properties): Either[Error, Properties] = {
      val errors = properties.fhlBucket.validatePeriod(period, properties.accountingPeriod)
      errors.map(Left(_)).getOrElse(Right(properties))
    }

    override def periodExists(periodId: PeriodId, properties: Properties): Boolean =
      period(periodId, properties).isDefined

    override def period(periodId: PeriodId, properties: Properties): Option[FHL.Properties] =
      properties.fhlBucket.periods.get(periodId)

    override def setPeriodsTo(periodId: PeriodId, period: FHL.Properties, properties: Properties): Properties =
      properties.copy(
        fhlBucket = properties.fhlBucket.copy(periods = properties.fhlBucket.periods.updated(periodId, period)))

    override def update(periodId: PeriodId, financials: FHL.Financials, properties: Properties): Properties = {
      val periodOpt = properties.fhlBucket.periods.find(period => period._1.equals(periodId))

      periodOpt.map { period =>
        setPeriodsTo(periodId, period._2.copy(financials = financials), properties)
      }.get
    }
  }
}
