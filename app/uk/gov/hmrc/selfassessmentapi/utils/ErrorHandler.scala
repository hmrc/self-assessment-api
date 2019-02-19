/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.utils


import javax.inject._
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

@Singleton
class ErrorHandler @Inject()(
                              env: Environment,
                              config: Configuration,
                              sourceMapper: OptionalSourceMapper,
                              router: Provider[Router]
                            ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override protected def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    super.onBadRequest(request, error).map { result =>
      error match {
        case "ERROR_INVALID_SOURCE_TYPE" => NotFound(toJson(ErrorNotFound))
        case "ERROR_TAX_YEAR_INVALID" => BadRequest(toJson(ErrorBadRequest(TAX_YEAR_INVALID, "Tax year invalid")))
        case "ERROR_NINO_INVALID" => BadRequest(toJson(ErrorBadRequest(NINO_INVALID, "The provided Nino is invalid")))
        case "ERROR_INVALID_DATE" => BadRequest(toJson(ErrorBadRequest(INVALID_DATE, "The provided dates are invalid")))
        case "ERROR_INVALID_DATE_FROM" => BadRequest(toJson(ErrorBadRequest(INVALID_DATE, "The from date in the query string is invalid")))
        case "ERROR_INVALID_DATE_TO" => BadRequest(toJson(ErrorBadRequest(INVALID_DATE, "The to date in the query string is invalid")))
        case "ERROR_EOPS_INVALID_DATE" => BadRequest(toJson(Errors.InvalidDate))
        case "ERROR_INVALID_DATE_RANGE" => BadRequest(toJson(ErrorBadRequest(INVALID_DATE_RANGE, "The date range in the query string is invalid")))
        case "ERROR_EOPS_INVALID_DATE_RANGE" => BadRequest(toJson(Errors.InvalidDateRange_2))
        case "ERROR_INVALID_PROPERTY_TYPE" => NotFound(toJson(ErrorNotFound))
        case _ => result
      }
    }
  }


  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    super.onServerError(request, ex).map { result =>
      ex match {
        case _ =>
          ex.getCause match {
            case ex: NotImplementedException => NotImplemented(toJson(ErrorNotImplemented))
            case _ => result
          }
      }
    }
  }

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    onServerError(request, exception)
  }
}
