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

import com.github.nscala_time.time.OrderingImplicits.LocalDateOrdering
import org.joda.time.{DateTimeZone, Duration, Interval}
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, Errors, Period, PeriodId}
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error

trait PeriodValidator[P <: Period] {

  val periods: Map[PeriodId, P]

  implicit private val ordering: Ordering[P] = Ordering.by(_.from)

  def validatePeriod(period: P, accountingPeriod: AccountingPeriod): Option[Errors.Error] = {
    if (containsPeriod(period).isDefined ||
      containsOverlappingPeriod(period) ||
      containsGap(period) ||
      containsMisalignedPeriod(period, accountingPeriod)) {
      Some(Error(INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", containsPeriod(period).orElse(Some(""))))
    }
    else None
  }

  private def containsPeriod(period: Period): Option[PeriodId] =
    periods.find { case (_, p) => p.from == period.from && p.to == period.to }.map(_._1)

  private def containsOverlappingPeriod(period: P): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    periods.exists { case (_, p) =>
      val existingPeriod = new Interval(p.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), p.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
      newPeriod.overlaps(existingPeriod) || newPeriod.abuts(existingPeriod)
    }
  }

  private def containsGap(period: P): Boolean = {
    val newPeriod = new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))

    val existingIntervals = periods.values.toSeq.sorted.reverse.map { period =>
      new Interval(period.from.toDateTimeAtStartOfDay(DateTimeZone.UTC), period.to.toDateTimeAtStartOfDay(DateTimeZone.UTC))
    }

    existingIntervals match {
      case Seq() => false
      case head +: _ => !head.gap(newPeriod).toDuration.isEqual(Duration.standardDays(1))
    }
  }

  private def containsMisalignedPeriod(period: P, accountingPeriod: AccountingPeriod): Boolean = {
    val alignedWithEnd = period.to.isBefore(accountingPeriod.end) || period.to.isEqual(accountingPeriod.end)

    if (periods.isEmpty) !(period.from.isEqual(accountingPeriod.start) && alignedWithEnd)
    else !alignedWithEnd
  }
}
