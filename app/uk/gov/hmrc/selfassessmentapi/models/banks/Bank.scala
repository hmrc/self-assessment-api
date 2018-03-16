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

package uk.gov.hmrc.selfassessmentapi.models.banks

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, SourceId}

case class Bank(id: Option[SourceId] = None,
                accountName: Option[String])

object Bank {

  private def lengthIsBetween(minLength: Int, maxLength: Int): Reads[String] =
    Reads.of[String].filter(ValidationError(s"field length must be between $minLength and $maxLength characters", ErrorCode.INVALID_FIELD_LENGTH)
    )(name => name.length <= maxLength && name.length >= minLength)

  private def isAlphaNumeric: Reads[String] =
    Reads.of[String].filter(ValidationError("field should only consist of alphanumeric characters", ErrorCode.INVALID_VALUE)
  )(_.matches("^([a-zA-Z0-9]+\\s?)+$"))

  private val validateAccountName: Reads[String] = lengthIsBetween(1, 32).andKeep(isAlphaNumeric)

  implicit val writes: Writes[Bank] = Json.writes[Bank]

  implicit val reads: Reads[Bank] =
    (__ \ "accountName").readNullable[String](validateAccountName).map(name => Bank(None, name))
}
