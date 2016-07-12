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


import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.ErrorCode


case class GenericError(code: ErrorCode, message: String)

case class ValidationError(code: ErrorCode, message: String, path: String)

case class CompositeError(code: ErrorCode, message: String, errors: Seq[ValidationError])

object GenericError {
  implicit val writes: Writes[GenericError] = Json.writes[GenericError]
}

object ValidationError {
  implicit val writes: Writes[ValidationError] = Json.writes[ValidationError]
}

object CompositeError {
  implicit val writes: Writes[CompositeError] = Json.writes[CompositeError]
}