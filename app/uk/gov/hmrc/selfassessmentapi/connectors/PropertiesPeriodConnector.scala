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

package uk.gov.hmrc.selfassessmentapi.connectors

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.{PeriodId, des}
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesPeriodResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesPeriodConnector {

  private lazy val baseUrl: String = AppContext.desUrl

  private def httpResponse2PropertiesPeriodResponse(fut: Future[HttpResponse],
                                                    from: Option[LocalDate] = None,
                                                    to: Option[LocalDate] = None): Future[PropertiesPeriodResponse] =
    fut.map(PropertiesPeriodResponse(_, from, to))

  def createFHL(nino: Nino, properties: FHL.Properties)(implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpResponse2PropertiesPeriodResponse(
      httpPost(baseUrl + s"/income-store/nino/$nino/uk-properties/furnished-holiday-lettings/periodic-summaries",
               des.properties.FHL.Properties.from(properties)),
      Some(properties.from),
      Some(properties.to))

  def createOther(nino: Nino, properties: Other.Properties)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpResponse2PropertiesPeriodResponse(
      httpPost(baseUrl + s"/income-store/nino/$nino/uk-properties/other/periodic-summaries",
               des.properties.Other.Properties.from(properties)),
      Some(properties.from),
      Some(properties.to))

  def retrieve(nino: Nino, periodId: PeriodId, propertyType: PropertyType)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpResponse2PropertiesPeriodResponse(
      httpGet(baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries/$periodId"))

  def retrieveAll(nino: Nino, propertyType: PropertyType)(
      implicit hc: HeaderCarrier): Future[PropertiesPeriodResponse] =
    httpResponse2PropertiesPeriodResponse(
      httpGet(baseUrl + s"/income-store/nino/$nino/uk-properties/$propertyType/periodic-summaries"))

}
