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
import uk.gov.hmrc.selfassessmentapi.models.{TaxYear, des}
import uk.gov.hmrc.selfassessmentapi.models.des.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{CharitableGivingsResponse, EmptyResponse}

import scala.concurrent.{ExecutionContext, Future}

object CharitableGivingsConnector extends CharitableGivingsConnector with ServicesConfig {
  override val baseUrl: String = AppContext.desUrl
}

trait CharitableGivingsConnector {

  val baseUrl: String

  def update(nino: Nino, taxYear: TaxYear, charitableGivings: CharitableGivings)(implicit hc:HeaderCarrier, ec: ExecutionContext): Future[EmptyResponse] = {
    httpPost[des.charitablegiving.CharitableGivings, EmptyResponse](s"$baseUrl/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}", charitableGivings, EmptyResponse)
  }

  def get(nino: Nino, taxYear: TaxYear)(implicit hc:HeaderCarrier, ec: ExecutionContext): Future[CharitableGivingsResponse] = {
    httpGet[CharitableGivingsResponse](s"$baseUrl/income-store/nino/$nino/charitable-giving/${taxYear.toDesTaxYear}", CharitableGivingsResponse)
  }
}
