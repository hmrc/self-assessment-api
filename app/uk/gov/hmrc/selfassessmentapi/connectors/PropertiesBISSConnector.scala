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
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.httpparsers.PropertiesBISSHttpParser

import scala.concurrent.{ExecutionContext, Future}

object PropertiesBISSConnector extends PropertiesBISSConnector {
  lazy val appContext = AppContext
  lazy val baseUrl: String = appContext.desUrl

  val http: WSHttp = WSHttp
}

trait PropertiesBISSConnector extends PropertiesBISSHttpParser {

  val baseUrl: String
  val http: HttpGet

  def getSummary(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PropertiesBISSOutcome] = {
    http.GET[PropertiesBISSOutcome](s"$baseUrl/self-assessment/ni/$nino/uk-properties/$taxYear/income-summary")(
      propertiesBISSHttpParser, hc, ex
    )
  }
}
