/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.obligations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class ObligationsQueryParams(from: LocalDate, to: LocalDate = LocalDate.now())

object ObligationsQueryParams {

  val MAX_PAST_RANGE_DAYS = 366

  implicit def toString(date: LocalDate) : String = {
    val dateFormat = "YYYY-MM-dd"
    date.format(DateTimeFormatter.ofPattern(dateFormat))
  }

  def apply() : ObligationsQueryParams = {
    val toDate = LocalDate.now()
    val fromDate = toDate.minusDays(MAX_PAST_RANGE_DAYS)
    ObligationsQueryParams(fromDate,toDate)
  }
}
