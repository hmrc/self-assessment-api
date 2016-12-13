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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.LiabilityService

import scala.concurrent.ExecutionContext.Implicits.global

object LiabilityController extends uk.gov.hmrc.selfassessmentapi.controllers.LiabilityController {

  override val context: String = AppContext.apiGatewayLinkContext

  private val liabilityService = LiabilityService()

  override def requestLiability(nino: Nino, taxYear: TaxYear) = Action.async { request =>
    liabilityService.calculate(nino, taxYear) map { _ => Accepted(JsObject(Nil)) }
  }

  override def retrieveLiability(nino: Nino, taxYear: TaxYear) = Action.async { request =>
    liabilityService.find(nino, taxYear) map {
      case Some(Left(error)) => Forbidden(Json.toJson(error))
      case Some(Right(liability)) => Ok(Json.toJson(liability))
      case _ => NotFound
    }
  }

}
