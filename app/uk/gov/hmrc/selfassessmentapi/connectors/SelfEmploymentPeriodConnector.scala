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
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.models.{PeriodId, SourceId, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentPeriodConnector {

  private lazy val baseUrl: String = AppContext.desUrl

  private def httpResponse2SeResponse(fut: Future[HttpResponse], from: Option[LocalDate] = None, to: Option[LocalDate] = None): Future[SelfEmploymentPeriodResponse] =
    fut.map(SelfEmploymentPeriodResponse(_, from, to))

  def create(nino: Nino, id: SourceId, selfEmploymentPeriod: SelfEmploymentPeriod)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpResponse2SeResponse(httpPost(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries",
             des.SelfEmploymentPeriod.from(selfEmploymentPeriod)), Some(selfEmploymentPeriod.from), Some(selfEmploymentPeriod.to))

  def get(nino: Nino, id: SourceId, periodId: PeriodId)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpResponse2SeResponse(httpGet(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries/$periodId"))

  def getAll(nino: Nino, id: SourceId)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpResponse2SeResponse(httpGet(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries"))

  def update(nino: Nino, id: SourceId, periodId: PeriodId, update: SelfEmploymentPeriodUpdate)(
      implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    httpResponse2SeResponse(httpPut(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries/$periodId",
            des.Financials.from(update)))

}
