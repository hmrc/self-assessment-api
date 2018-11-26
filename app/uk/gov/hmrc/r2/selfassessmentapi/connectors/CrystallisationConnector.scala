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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.models.crystallisation.CrystallisationRequest
import uk.gov.hmrc.r2.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.{CrystObligationsResponse, CrystallisationIntentResponse, EmptyResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object CrystallisationConnector extends CrystallisationConnector {
  lazy val appContext = AppContext
  val baseUrl: String = appContext.desUrl
}

trait CrystallisationConnector extends BaseConnector {
  protected val baseUrl: String

  def intentToCrystallise(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CrystallisationIntentResponse] =
    httpEmptyPost[CrystallisationIntentResponse](
      baseUrl + s"/income-tax/nino/$nino/taxYear/${taxYear.toDesTaxYear}/tax-calculation?crystallise=true",
      CrystallisationIntentResponse)

  def crystallise(nino: Nino, taxYear: TaxYear, request: CrystallisationRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmptyResponse] =
    httpEmptyPost[EmptyResponse](
      baseUrl + s"/income-tax/calculation/nino/$nino/${taxYear.toDesTaxYear}/${request.calculationId}/crystallise",
      EmptyResponse)

  def get(nino: Nino, queryParams: ObligationQueryParams)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CrystObligationsResponse] = {
    val queryString = (queryParams.from.fold("")(date => s"from=$date&") +
                       queryParams.to.fold("")(date => s"to=$date&") +
                       queryParams.status.fold("")(status => s"status=$status")).stripSuffix("&")
    httpGet[CrystObligationsResponse](baseUrl + s"/enterprise/obligation-data/nino/$nino/ITSA?$queryString", CrystObligationsResponse)
  }
}
