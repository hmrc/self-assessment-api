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
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{CrystallisationIntentResponse, EmptyResponse}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.selfassessmentapi.models.crystallisation.CrystallisationRequest
import uk.gov.hmrc.selfassessmentapi.models.des.crystallisation.Crystallisation
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.RequestDateTime

object CrystallisationConnector {

  private lazy val baseUrl: String = AppContext.desUrl

  def intentToCrystallise(nino: Nino, taxYear: TaxYear, requestTimestamp: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CrystallisationIntentResponse] =
    httpPost[RequestDateTime, CrystallisationIntentResponse](
      baseUrl + s"/income-tax-self-assessment/nino/$nino/taxYear/${taxYear.toDesTaxYear}/intent-to-crystallise", RequestDateTime(requestTimestamp),
      CrystallisationIntentResponse)

  def crystallise(nino: Nino, taxYear: TaxYear, request: CrystallisationRequest, requestTimestamp: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmptyResponse] =
    httpPost[Crystallisation, EmptyResponse](
      baseUrl + s"/income-tax-self-assessment/nino/$nino/taxYear/${taxYear.toDesTaxYear}/crystallise",
      Crystallisation(request.calculationId, requestTimestamp),
      EmptyResponse)

}
