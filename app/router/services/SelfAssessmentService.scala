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

import com.google.inject.name.Names
import javax.inject.Inject
import play.api.Application
import play.api.inject.{BindingKey, QualifierInstance}
import play.api.libs.json.JsValue
import play.api.mvc.Request
import router.connectors.{BaseConnector, SelfAssessmentConnector}
import router.constants.Versions._
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SelfAssessmentService @Inject()(app: Application) extends SelfAssessmentServiceT {
  def connector(version: String): BaseConnector =
    app.injector.instanceOf(BindingKey(classOf[SelfAssessmentConnector], Some(QualifierInstance(Names.named(s"self-assessment-$version")))))
}

trait SelfAssessmentServiceT extends Service {

  def connector(version: String): BaseConnector

  def get()(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {
    withApiVersion{
      case Some(`1.0`) => connector(`1.0`).get(req.uri)
    }
  }

  def post(body: JsValue)(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {
    withApiVersion{
      case Some(`1.0`) => connector(`1.0`).post(req.uri, body)
    }
  }

  def put(body: JsValue)(implicit hc: HeaderCarrier, req: Request[_]): Future[SelfAssessmentOutcome] = {
    withApiVersion{
      case Some(`1.0`) => connector(`1.0`).put(req.uri, body)
    }
  }
}