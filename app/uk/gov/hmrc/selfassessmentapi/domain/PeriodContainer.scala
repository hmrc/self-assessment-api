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

import com.github.nscala_time.time.OrderingImplicits.LocalDateOrdering
import org.joda.time.{DateTimeZone, Duration, Interval}
import play.api.libs.json.Format
import uk.gov.hmrc.selfassessmentapi.controllers.api.PeriodId
import uk.gov.hmrc.selfassessmentapi.resources.models.{Period, PeriodicData}

trait PeriodContainer[P <: Period, PC, PD <: PeriodicData] {

  implicit val ordering: Ordering[P] = Ordering.by(_.from)

  val periods: Map[PeriodId, P]

  def containsPeriod(period: Period): Boolean =
    periods.exists { case (_, p) => p.from == period.from && p.to == period.to }
  def periodExists(periodId: PeriodId): Boolean = period(periodId).nonEmpty
  def period(periodId: PeriodId): Option[P] = periods.get(periodId)

  def setPeriodsTo(periodId: PeriodId, period: P): PC
  def update(periodId: PeriodId, periodicData: PD): PC

  def containsGap(period: P): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    val existingIntervals = periods.values.toSeq.sorted.reverse.map { period =>
      new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
    }

    existingIntervals match {
      case Seq() => false
      case head +: _ => !head.gap(newPeriod).toDuration.isEqual(Duration.standardDays(1))
    }
  }

  def containsOverlappingPeriod(period: P): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    periods.exists { case (_, p) =>
      val existingPeriod = new Interval(p.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), p.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
      newPeriod.overlaps(existingPeriod) || newPeriod.abuts(existingPeriod)
    }
  }

  def containsMisalignedPeriod(period: P): Boolean

}
