/*
 * Copyright 2021 HM Revenue & Customs
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

package router.services

import config.AppConfig

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.Request
import router.connectors.SelfAssessmentConnector
import router.constants.Versions
import router.constants.Versions.{VERSION_1, VERSION_2}
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ReleaseTwoService @Inject()(val appConfig: AppConfig,
                                  val selfAssessmentConnector: SelfAssessmentConnector) extends Service {

  val r2 = "/r2"

  def create(body: JsValue)(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {


    withApiVersion {
      case Some(VERSION_1) => selfAssessmentConnector.post(r2+req.uri, body)
      case Some(VERSION_2) => selfAssessmentConnector.post(r2+req.uri, body)(Versions.convertHeaderToVersion1)
    }
  }

  def get()(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {

    withApiVersion {
      case Some(VERSION_1) => selfAssessmentConnector.get(r2+req.uri)
      case Some(VERSION_2) => selfAssessmentConnector.get(r2+req.uri)(Versions.convertHeaderToVersion1)
    }
  }

  def amend(body: JsValue)(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {

    withApiVersion {
      case Some(VERSION_1) => selfAssessmentConnector.put(r2+req.uri, body)
      case Some(VERSION_2) => selfAssessmentConnector.put(r2+req.uri, body)(Versions.convertHeaderToVersion1)
    }
  }

}
