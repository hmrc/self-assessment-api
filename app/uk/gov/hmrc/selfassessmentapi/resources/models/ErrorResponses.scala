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

import play.api.libs.json.Json
import uk.gov.hmrc.api.controllers.ErrorResponse
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode.ErrorCode

case object ErrorNotImplemented extends ErrorResponse(501, "NOT_IMPLEMENTED", "The resource is not implemented")

case object ErrorFeatureSwitched extends ErrorResponse(400, "INVALID_REQUEST", "The provided JSON object contains disabled properties")

case class ErrorBadRequest(code: ErrorCode, override val message: String)
  extends ErrorResponse(400, code.toString, message)

case class InvalidPart(code: ErrorCode, message: String, path : String)

object InvalidPart {
  implicit val writes = Json.writes[InvalidPart]
}

case class InvalidRequest(code: ErrorCode, message: String, errors: Seq[InvalidPart])

object InvalidRequest {
  implicit val writes = Json.writes[InvalidRequest]
}

case class LiabilityError(code: ErrorCode, message: String)

object LiabilityError {
  implicit val writes = Json.writes[LiabilityError]
}

case class LiabilityErrors(code: ErrorCode, message: String, errors: Seq[LiabilityError])

object LiabilityErrors {
  implicit val writes = Json.writes[LiabilityErrors]
}


