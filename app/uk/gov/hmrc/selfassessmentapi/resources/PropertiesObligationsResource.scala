/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.services.PropertiesObligationsService

import scala.concurrent.ExecutionContext.Implicits.global

object PropertiesObligationsResource extends BaseResource {
  private val FeatureSwitch = FeatureSwitchAction(SourceType.Properties, "obligations")
  private val propertiesService = PropertiesObligationsService

  def retrieveObligations(nino: Nino) = FeatureSwitch.async(parse.empty) { implicit headers =>
    withAuth(nino) {
      propertiesService.retrieveObligations(nino, headers.headers.get(GovTestScenarioHeader)) map {
        case Some(obligations) => Ok(Json.toJson(obligations))
        case None => NotFound
      }
    }
  }
}
