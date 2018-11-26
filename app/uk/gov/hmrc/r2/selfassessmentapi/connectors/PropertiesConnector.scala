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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.NewProperties
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.PropertiesResponse

import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

object PropertiesConnector extends PropertiesConnector {
  lazy val appContext = AppContext
  lazy val baseUrl: String = appContext.desUrl
}

trait PropertiesConnector extends BaseConnector{

  val baseUrl: String

  def create(nino: Nino, properties: NewProperties)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertiesResponse] = {
    httpPost[NewProperties, PropertiesResponse](
      baseUrl + s"/income-tax-self-assessment/nino/$nino/properties",
      properties,
      PropertiesResponse)
  }

  def retrieve(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertiesResponse] =
    httpGet[PropertiesResponse](baseUrl + s"/registration/business-details/nino/$nino", PropertiesResponse)

}
