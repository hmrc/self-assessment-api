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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import cats.implicits._
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesConnector
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.NewProperties
import uk.gov.hmrc.r2.selfassessmentapi.services.AuthorisationService

//object PropertiesResource extends PropertiesResource {
//  override val appContext = AppContext
//  override val authService = AuthorisationService
//  override val propertiesConnector = PropertiesConnector
//}

class PropertiesResource @Inject()(
                                    override val appContext: AppContext,
                                    override val authService: AuthorisationService,
                                    propertiesConnector: PropertiesConnector
                                  ) extends BaseResource {
  //  val propertiesConnector: PropertiesConnector

  def create(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Properties).async(parse.json) { implicit request => {
      for {
        props <- validateJson[NewProperties](request.body)
        response <- execute { _ => propertiesConnector.create(nino, props) }
      } yield response
    } onDesSuccess { response =>
      response.filter {
        case 200 => Created.withHeaders(LOCATION -> response.createLocationHeader(nino))
        case 403 => Conflict.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/uk-properties")
      }
    } recoverWith exceptionHandling
    }

  def retrieve(nino: Nino): Action[AnyContent] =
    APIAction(nino, SourceType.Properties).async { implicit request => {
      for {
        response <- execute { _ => propertiesConnector.retrieve(nino) }
      } yield response
    } onDesSuccess { response =>
      response.filter {
        case 200 =>
          response.property match {
            case Some(property) => Ok(Json.toJson(property))
            case None => NotFound
          }
      }
    } recoverWith exceptionHandling

    }

}
