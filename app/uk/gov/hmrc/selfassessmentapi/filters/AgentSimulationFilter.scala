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

package uk.gov.hmrc.selfassessmentapi.filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.selfassessmentapi.config.simulation.{AgentAuthorizationSimulation, AgentSubscriptionSimulation, ClientSubscriptionSimulation}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentSimulationFilter @Inject()(implicit val mat: Materializer, appContext: AppContext) extends Filter {

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    val method = rh.method

    val featureSwitch = FeatureSwitch(appContext.featureSwitch, appContext.env)

    if (featureSwitch.isAgentSimulationFilterEnabled) {
      rh.headers.get(GovTestScenarioHeader) match {
        case Some("AGENT_NOT_SUBSCRIBED") => AgentSubscriptionSimulation(f, rh, method)
        case Some("AGENT_NOT_AUTHORIZED") => AgentAuthorizationSimulation(f, rh, method)
        case Some("CLIENT_NOT_SUBSCRIBED") => ClientSubscriptionSimulation(f, rh, method)
        case _ => f(rh)
      }
    } else {
      f(rh)
    }

  }

}
