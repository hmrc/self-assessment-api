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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json.toJson
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.CrystallisationConnector
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear}


object CrystallisationResource extends BaseResource {

  private val connector = CrystallisationConnector

  def intentToCrystallise(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Crystallisation).async { implicit request =>
      val requestTimestamp: String = getRequestTimestamp
      connector.intentToCrystallise(nino, taxYear, requestTimestamp) map { response =>
        response.filter {
          case 200 =>
            val contextPrefix = if (AppContext.registrationEnabled) "/self-assessment" else ""
            val url = response.calculationId.map(id => s"$contextPrefix/ni/$nino/calculations/$id").getOrElse("")
            SeeOther(url).withHeaders(LOCATION -> url)
          case 403 if response.errorCodeIs(REQUIRED_END_OF_PERIOD_STATEMENT) => Forbidden(toJson(Errors.businessError(Errors.RequiredEndOfPeriodStatement)))

        }
      }
    }

}
