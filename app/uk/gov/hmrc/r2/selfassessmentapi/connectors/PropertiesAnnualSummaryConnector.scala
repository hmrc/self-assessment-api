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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.models.des.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary}
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertiesAnnualSummary
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.{des, properties}
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.PropertiesAnnualSummaryResponse
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear

import scala.concurrent.{ExecutionContext, Future}

//object PropertiesAnnualSummaryConnector extends PropertiesAnnualSummaryConnector {
//  lazy val appContext = AppContext
//  lazy val baseUrl: String = appContext.desUrl
//}

class PropertiesAnnualSummaryConnector @Inject()(
                                                  override val appContext: AppContext,
                                                  override val http: DefaultHttpClient
                                                ) extends BaseConnector{
  val baseUrl: String = appContext.desUrl

  def update(nino: Nino, propertyType: PropertyType, taxYear: TaxYear, update: PropertiesAnnualSummary)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertiesAnnualSummaryResponse] = {
    val url
    : String = baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}"
    update match {
      case other: properties.OtherPropertiesAnnualSummary =>
        httpPut[OtherPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](
          url,
          des.OtherPropertiesAnnualSummary.from(other),
          PropertiesAnnualSummaryResponse(propertyType, _))
      case fhl: properties.FHLPropertiesAnnualSummary =>
        httpPut[FHLPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](
          url,
          des.FHLPropertiesAnnualSummary.from(fhl),
          PropertiesAnnualSummaryResponse(propertyType, _))
    }
  }

  def get(nino: Nino, propertyType: PropertyType, taxYear: TaxYear)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertiesAnnualSummaryResponse] =
    httpGet[PropertiesAnnualSummaryResponse](
      baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/annual-summaries/${taxYear.toDesTaxYear}",
      PropertiesAnnualSummaryResponse(propertyType, _))
}
