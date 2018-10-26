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

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.selfassessmentapi.httpparsers.SelfEmploymentBISSHttpParser
import uk.gov.hmrc.selfassessmentapi.httpparsers.SelfEmploymentBISSHttpParser.SelfEmploymentBISSOutcome
import uk.gov.hmrc.selfassessmentapi.models.TaxYear

import scala.concurrent.{ExecutionContext, Future}

object SelfEmploymentBISSConnector extends SelfEmploymentBISSConnector {
  lazy val appContext = AppContext
  lazy val baseUrl: String = appContext.desUrl

  val http: WSHttp = WSHttp
}

trait SelfEmploymentBISSConnector extends SelfEmploymentBISSHttpParser with BaseConnector {

  val baseUrl: String
  val http: HttpGet
  val logger: Logger = Logger(this.getClass.getSimpleName)

  def getSummary(nino: Nino, taxYear: TaxYear, selfEmploymentId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentBISSOutcome] = {

    logger.debug(s"[SelfEmploymentBISSConnector][getSummary] Get BISS for NI number: $nino with selfEmploymentId: $selfEmploymentId")

    http.GET[SelfEmploymentBISSOutcome](s"$baseUrl/income-tax/income-sources/nino/$nino/self-employment/${taxYear.toDesTaxYear}/biss?incomesourceid=$selfEmploymentId")(
      selfEmploymentBISSHttpParser, withDesHeaders(hc), ec)
  }
}
