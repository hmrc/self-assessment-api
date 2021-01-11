/*
 * Copyright 2021 HM Revenue & Customs
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

package router.errors

import play.api.libs.json.{JsValue, Json, OWrites}

case class ErrorCode(code: String, message: String){
  def asJson(implicit wts: OWrites[ErrorCode]): JsValue = Json.toJson(this)
}

object ErrorCode {
  implicit val writes: OWrites[ErrorCode] = Json.writes[ErrorCode]

  val invalidBearerToken = ErrorCode("UNAUTHORIZED", "Bearer token is missing or not authorized")
  val invalidAcceptHeader = ErrorCode("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")
  val notFound = ErrorCode("NOT_FOUND", "The requested resource could not be found.")
  val internalServerError = ErrorCode("INTERNAL_SERVER_ERROR", "An internal server error occurred")
  val unauthorisedError = ErrorCode("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised.")
  val invalidRequest = ErrorCode("INVALID_REQUEST", "Invalid request")
  val invalidBodyType = ErrorCode("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
  val matchingResourceNotFound = ErrorCode("MATCHING_RESOURCE_NOT_FOUND", "The requested resource could not be found.")
}
