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

package uk.gov.hmrc.r2.selfassessmentapi.config.simulation

import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Status
import uk.gov.hmrc.r2.selfassessmentapi.models.{ErrorAgentNotAuthorized, ErrorAgentBadRequest}

import scala.concurrent.{ExecutionContext, Future}

object AgentAuthorizationSimulation extends Simulation {
  override def apply(f: RequestHeader => Future[Result], rh: RequestHeader, method: String)(implicit ec: ExecutionContext): Future[Result] = {
    method match {
      case "GET" =>
        Future.successful(
          Status(ErrorAgentNotAuthorized.httpStatusCode)(
            Json.toJson(ErrorAgentNotAuthorized))
        )
      case "PUT" =>
        Future.successful(
          Status(ErrorAgentNotAuthorized.httpStatusCode)(
            Json.toJson(ErrorAgentNotAuthorized))
        )
      case "POST" =>
        Future.successful(
          Status(ErrorAgentNotAuthorized.httpStatusCode)(
            Json.toJson(ErrorAgentNotAuthorized))
        )
      case _ => f(rh).map { res =>
        if (res.header.status / 100 == 2 || res.header.status == 400) res
        else Status(ErrorAgentBadRequest.httpStatusCode)(Json.toJson(ErrorAgentBadRequest))
      }
    }
  }
}
