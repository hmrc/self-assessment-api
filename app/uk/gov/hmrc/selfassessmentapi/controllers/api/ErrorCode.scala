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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson

object ErrorCode extends Enumeration {
  type ErrorCode = Value
  val
    // TODO check name is OK
  INVALID_REQUEST,
  INVALID_FIELD,
  TAX_YEAR_INVALID,
  MAX_FIELD_LENGTH_EXCEEDED,
  INVALID_MONETARY_AMOUNT,
  INVALID_TAX_DEDUCTION_AMOUNT,
  INVALID_DISALLOWABLE_AMOUNT,
  DEPRECIATION_DISALLOWABLE_AMOUNT,
  COMMENCEMENT_DATE_NOT_IN_THE_PAST,
  MISSING_COUNTRY,
  MISSING_REGISTRATION_AUTHORITY,
  MUST_BE_BLIND_TO_WANT_SPOUSE_TO_USE_SURPLUS_ALLOWANCE,
  MAX_MONETARY_AMOUNT,
  NO_VALUE_FOUND,
  MAXIMUM_AMOUNT_EXCEEDED,
  VALUE_BELOW_MINIMUM,
  BENEFIT_STOPPED_DATE_INVALID,
  UNDEFINED_REQUIRED_ELEMENT,
  ONLY_PENSION_CONTRIBUTIONS_SUPPORTED,
  INVALID_EMPLOYMENT_TAX_PAID,
  LIABILITY_CALCULATION_ERROR,
  INVALID_TYPE = Value

  implicit val format = EnumJson.enumFormat(ErrorCode, Some("ErrorCode is invalid"))
}

