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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.libs.json.Format

object ErrorCode extends Enumeration {
  type ErrorCode = Value
  val
  INVALID_REQUEST,
  INVALID_FIELD_LENGTH,
  INVALID_FIELD_FORMAT,
  INVALID_MONETARY_AMOUNT,
  INVALID_TAX_DEDUCTION_AMOUNT,
  INVALID_DISALLOWABLE_AMOUNT,
  DEPRECIATION_DISALLOWABLE_AMOUNT,
  DATE_NOT_IN_THE_PAST,
  START_DATE_INVALID,
  INVALID_VALUE,
  INVALID_TYPE,
  INVALID_PERIOD,
  INVALID_ACCOUNTING_PERIOD,
  ALREADY_EXISTS,
  TOO_MANY_SOURCES,
  INVALID_BALANCING_CHARGE_BPRA,
  NOT_IMPLEMENTED,
  TAX_YEAR_INVALID,
  NINO_INVALID,
  AGENT_NOT_SUBSCRIBED,
  AGENT_NOT_AUTHORIZED,
  CLIENT_NOT_SUBSCRIBED,
  INVALID_BUSINESS_DESCRIPTION,
  INVALID_POSTCODE,
  NO_INCOMES_AND_EXPENSES = Value

  implicit val format: Format[ErrorCode] = EnumJson.enumFormat(ErrorCode, Some("ErrorCode is invalid"))
}
