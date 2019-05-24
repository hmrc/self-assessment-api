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

package config

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val environment: Environment,
                          val config: Configuration) extends ServicesConfig {

  override protected def mode: Mode = environment.mode
  override protected def runModeConfiguration: Configuration = config

  def appName: String = getString("appName")
  def appUrl: String = getString("appUrl")
  def registrationEnabled: Boolean = getBoolean("microservice.services.service-locator.enabled")

  def featureSwitch: Option[Configuration] = config.getConfig(s"feature-switch")
  def apiStatus(version: String): String = getString(s"api.$version.status")
  def apiGatewayContext: String = getString("api.gateway.context")

  //Services
  def saApiUrl: String = baseUrl("self-assessment-api")
  def cgApiUrl: String = baseUrl("mtd-charitable-giving")
  def taxCalcUrl: String = baseUrl("mtd-tax-calculation")
  def propertyUrl: String = baseUrl("mtd-property-api")
  def selfEmploymentUrl: String = baseUrl("mtd-self-employment")
  def release2Enabled: Boolean = getBoolean("release-2.enabled")
  def dividendsApiUrl: String = baseUrl("mtd-dividends-income")
  def savingsAccountApiUrl: String = baseUrl("mtd-savings-accounts")
  def crystallisationApiUrl: String = baseUrl("mtd-crystallisation")

}
