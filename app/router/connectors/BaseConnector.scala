/*
 * Copyright 2019 HM Revenue & Customs
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

package router.connectors

import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Request
import router.httpParsers.SelfAssessmentHttpParser
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait BaseConnector {

  val http: HttpClient
  val httpParser: SelfAssessmentHttpParser
  val appConfig: AppConfig

  val serviceUrl: String

  def get(uri: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[SelfAssessmentOutcome] = {
    http.GET[SelfAssessmentOutcome](s"$serviceUrl$uri")(httpParser, hc, implicitly)
  }

  def post(uri: String, body: JsValue)(implicit hc: HeaderCarrier, request: Request[_]): Future[SelfAssessmentOutcome] = {
    http.POST[JsValue, SelfAssessmentOutcome](s"$serviceUrl$uri", body)(implicitly, httpParser, hc, implicitly)
  }

  def put(uri: String, body: JsValue)(implicit hc: HeaderCarrier, request: Request[_]): Future[SelfAssessmentOutcome] = {
    http.PUT[JsValue, SelfAssessmentOutcome](s"$serviceUrl$uri", body)(implicitly, httpParser, hc, implicitly)
  }
}
