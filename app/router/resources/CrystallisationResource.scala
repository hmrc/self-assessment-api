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

package router.resources

import config.AppConfig
import javax.inject.Inject
import play.api.libs.json.{JsNull, JsValue}
import play.api.mvc.{Action, AnyContent, BodyParser, ControllerComponents}
import router.constants.Versions
import router.constants.Versions._
import router.services.{CrystallisationService, Service}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext.Implicits.global

class CrystallisationResource @Inject()(service: CrystallisationService,
                                        val cc: ControllerComponents,
                                        val authConnector: AuthConnector)(implicit appConfig: AppConfig) extends BaseResource(cc, authConnector) with Service {

  def post(param: Any*): Action[JsValue] = AuthAction.async(parse.json) {
    implicit request =>
      withJsonBody[JsValue] {
        service.post(_).map {
          case Left(error) => buildErrorResponse(error)
          case Right(apiResponse) => buildResponse(apiResponse)
        }
      }
  }

  // Note that intent V1 requires empty JSON (i.e. {}) whereas V2 requires completely empty body. So we need to parse
  // accordingly these with the empty body parsed to JsNull
  private val jsonOrEmptyParser: BodyParser[JsValue] = parse.using { request =>

    if (Versions.getFromRequest(request).contains(VERSION_1))
      parse.json
    else
      parse.empty.map(_ => JsNull)
  }

  def intent(param: Any*): Action[JsValue] = AuthAction.async(jsonOrEmptyParser) { implicit request =>
    withJsonBody[JsValue] { body =>
      val serviceOutcome = body match {
        case JsNull => service.postEmpty
        case json => service.post(json)
      }

      serviceOutcome.map {
        case Left(error) => buildErrorResponse(error)
        case Right(apiResponse) => buildResponse(apiResponse)
      }
    }
  }

  def get(param: Any*): Action[AnyContent] = {
    AuthAction.async {
      implicit request =>
        service.get().map{
          case Left(error) => buildErrorResponse(error)
          case Right(apiResponse) => buildResponse(apiResponse)
        }
    }
  }
}

