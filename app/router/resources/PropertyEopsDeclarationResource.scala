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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import router.services.PropertyEopsDeclarationService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

class PropertyEopsDeclarationResource @Inject()(service: PropertyEopsDeclarationService,
                                                val cc: ControllerComponents,
                                                val authConnector: AuthConnector)
                                               (implicit ec: ExecutionContext, appConfig: AppConfig) extends BaseResource(cc, authConnector) {

  def post(nino: String, from: String, to: String): Action[JsValue] = AuthAction.async(parse.json) {
    implicit request =>
      withJsonBody[JsValue]{
        service.post(_).map {
          case Left(error) => buildErrorResponse(error)
          case Right(apiResponse) => buildResponse(apiResponse)
        }
      }

  }
}
