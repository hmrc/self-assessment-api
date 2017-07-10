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
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.Financials
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.models.{PeriodId, SourceId, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse

import scala.concurrent.Future

object SelfEmploymentPeriodConnector {

  private lazy val baseUrl: String = AppContext.desUrl

  def create(nino: Nino, id: SourceId, selfEmploymentPeriod: SelfEmploymentPeriod)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpPost[selfemployment.SelfEmploymentPeriod, SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries",
      selfemployment.SelfEmploymentPeriod.from(selfEmploymentPeriod),
      SelfEmploymentPeriodResponse)

  def get(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpGet[SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries?from=$from&to=$to",
      SelfEmploymentPeriodResponse)

  def getAll(nino: Nino, id: SourceId)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpGet[SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries",
      SelfEmploymentPeriodResponse)

  def update(nino: Nino, id: SourceId, from: LocalDate, to: LocalDate, update: SelfEmploymentPeriodUpdate)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpPut[Financials, SelfEmploymentPeriodResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries?from=$from&to=$to",
      selfemployment.Financials.from(update),
      SelfEmploymentPeriodResponse)

}
