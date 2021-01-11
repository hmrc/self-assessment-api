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
import router.connectors.{CrystallisationConnector, SelfAssessmentConnector}
import router.constants.Versions._
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class CrystallisationService @Inject() (val appConfig: AppConfig,
                                        val crystallisationConnector: CrystallisationConnector,
                                        val selfAssessmentConnector: SelfAssessmentConnector
                                       ) extends Service {

  def post(body: JsValue)(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {

    withApiVersion {
      case Some(VERSION_2) => crystallisationConnector.post(s"/$VERSION_2${req.uri}", body)
    }
  }

  def postEmpty(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {

    withApiVersion {
      case Some(VERSION_2) => crystallisationConnector.postEmpty(s"/$VERSION_2${req.uri}")
    }
  }

  def get()(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {
    withApiVersion {
      case Some(VERSION_1) => selfAssessmentConnector.get(req.uri)
      case Some(VERSION_2) => crystallisationConnector.get(s"/$VERSION_2${req.uri}")}
  }
}
