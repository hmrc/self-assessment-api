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

package uk.gov.hmrc.selfassessmentapi.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.Period
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.RequestDateTime
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse

import scala.concurrent.{ExecutionContext, Future}

object PropertiesPeriodStatementConnector extends PropertiesPeriodStatementConnector with ServicesConfig{
  val baseUrl = AppContext.desUrl
}

trait PropertiesPeriodStatementConnector {

  val baseUrl: String

  def create(nino: Nino, accountingPeriod: Period, requestTimestamp: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmptyResponse] =
    httpPost[RequestDateTime, EmptyResponse](s"$baseUrl/income-store/nino/$nino/uk-properties/accounting-periods/${accountingPeriod.periodId}/statement",
      RequestDateTime(requestTimestamp), EmptyResponse)
}
