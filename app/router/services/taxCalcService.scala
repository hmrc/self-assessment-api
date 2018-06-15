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

package router.services

import javax.inject.{Inject, Provider}
import play.api.Application
import play.api.mvc.Request
import router.connectors.{BaseConnector, SelfAssessmentConnector, TaxCalcConnector}
import router.constants.Versions._
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class TaxCalcServiceImpl @Inject()(app: Provider[Application],
                                   val selfAssessmentConnector: SelfAssessmentConnector) extends TaxCalcService {

  def taxCalcConnector(version: String): BaseConnector =
    app.get.injector.instanceOf(injectedConnector(classOf[TaxCalcConnector], s"tax-calc-$version"))
}

trait TaxCalcService extends Service {

  def selfAssessmentConnector: SelfAssessmentConnector

  def taxCalcConnector(version: String): BaseConnector

  def get()(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {
    withApiVersion {
      case Some(VERSION_1) => selfAssessmentConnector.get(req.uri)
      case Some(VERSION_2) => taxCalcConnector(VERSION_2).get(s"$VERSION_2/${req.uri}")
    }
  }
}
