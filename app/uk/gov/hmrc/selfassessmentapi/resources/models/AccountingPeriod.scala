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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class AccountingPeriod(start: LocalDate, end: LocalDate)

object AccountingPeriod {
  implicit val writes: Writes[AccountingPeriod] = Json.writes[AccountingPeriod]

  implicit val reads: Reads[AccountingPeriod] = (
    (__ \ "start").read[LocalDate](startDateValidator) and
    (__ \ "end").read[LocalDate]
    )(AccountingPeriod.apply _)
    .filter(ValidationError("the accounting period 'start' date should come before the 'end' date", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    )(accountingPeriodValidator)

  private def accountingPeriodValidator(accountingPeriod: AccountingPeriod) = {
    accountingPeriod.start.isBefore(accountingPeriod.end) || accountingPeriod.start.isEqual(accountingPeriod.end)
  }

  private def startDateValidator = Reads.of[LocalDate].filter(
    ValidationError("the 'start' should be after or equal to 2017-04-01", ErrorCode.DATE_NOT_IN_THE_PAST)
  )(date => {
    val mvpStart = LocalDate.parse("2017-04-01")
    date.isAfter(mvpStart) || date.isEqual(mvpStart)
  })
}
