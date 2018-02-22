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
import uk.gov.hmrc.selfassessmentapi.models.des.giftaid.GiftAidPayments
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{EmptyResponse, GiftAidPaymentsResponse}

import scala.concurrent.{ExecutionContext, Future}

object GiftAidPaymentsConnector extends GiftAidPaymentsConnector with ServicesConfig {
  override val baseUrl: String = AppContext.desUrl
}

trait GiftAidPaymentsConnector {

  val baseUrl: String

  def update(nino: Nino, taxYear: TaxYear, giftAidPayments: GiftAidPayments)(implicit hc:HeaderCarrier, ec: ExecutionContext): Future[EmptyResponse] = {
    httpPost[des.giftaid.GiftAidPayments, EmptyResponse](s"$baseUrl/income-store/nino/$nino/gift-aid/${taxYear.toDesTaxYear}", giftAidPayments, EmptyResponse)
  }

  def get(nino: Nino, taxYear: TaxYear)(implicit hc:HeaderCarrier, ec: ExecutionContext): Future[GiftAidPaymentsResponse] = {
    httpGet[GiftAidPaymentsResponse](s"$baseUrl/income-store/nino/$nino/gift-aid/${taxYear.toDesTaxYear}", GiftAidPaymentsResponse)
  }
}
