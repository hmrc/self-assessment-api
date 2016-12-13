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

package uk.gov.hmrc.selfassessmentapi.resources.models

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate

trait PeriodValidator[P <: Period] {
  protected def periodDateValidator(period: Period): Boolean =
    period.from.isBefore(period.to) || period.from.isEqual(period.to)

  implicit val dateTimeOrder: Ordering[LocalDate] = OrderingImplicits.LocalDateOrdering
  implicit val order: Ordering[P] = Ordering.by(_.from)
}
