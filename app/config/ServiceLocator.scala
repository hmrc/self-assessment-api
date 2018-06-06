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

package config

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.api.config.{ServiceLocatorConfig, ServiceLocatorRegistration}
import uk.gov.hmrc.api.connector.ServiceLocatorConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

@Singleton
class ServiceLocator @Inject()(val environment: Environment,
                               val conf: Configuration,
                               val WSHttp: HttpClient)
  extends ServiceLocatorRegistration with ServiceLocatorConfig {

  override protected def runModeConfiguration: Configuration = conf

  protected val mode: Mode = environment.mode

  override val slConnector: ServiceLocatorConnector = ServiceLocatorConnector(WSHttp)
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}
