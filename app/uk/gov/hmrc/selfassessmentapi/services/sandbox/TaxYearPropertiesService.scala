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

package uk.gov.hmrc.selfassessmentapi.services.sandbox

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearProperties
import uk.gov.hmrc.selfassessmentapi.services.SwitchedTaxYearProperties

import scala.concurrent.Future

class TaxYearPropertiesService(override val featureSwitch: FeatureSwitch) extends SwitchedTaxYearProperties {
  def findTaxYearProperties(nino: Nino, taxYear: TaxYear): TaxYearProperties = {
    switchedTaxYearProperties(TaxYearProperties.example())
  }

  def updateTaxYearProperties(nino: Nino, taxYear: TaxYear, taxYearProperties: TaxYearProperties): Future[Boolean] = {
    val switchedProperties = switchedTaxYearProperties(taxYearProperties)

    // If nothing has been removed (i.e. switched off), update, otherwise return an error.
    val isValidProperties = switchedProperties == taxYearProperties

    Future.successful(isValidProperties)
  }
}

object TaxYearPropertiesService {
  private val taxYearPropertiesService = new TaxYearPropertiesService(FeatureSwitch(AppContext.featureSwitch))
  def apply() = taxYearPropertiesService
}
