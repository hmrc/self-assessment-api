/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val environment: Environment,
                          val configuration: Configuration,
                          config: ServicesConfig) {

  def appName: String = config.getString("appName")
  def appUrl: String = config.getString("appUrl")
  def registrationEnabled: Boolean = config.getBoolean("microservice.services.service-locator.enabled")

  def featureSwitch: Option[Configuration] = configuration.getOptional[Configuration](s"feature-switch")
  def apiStatus(version: String): String = config.getString(s"api.$version.status")
  def apiGatewayContext: String = config.getString("api.gateway.context")

  //Services
  def saApiUrl: String = config.baseUrl("self-assessment-api")
  def cgApiUrl: String = config.baseUrl("mtd-charitable-giving")
  def taxCalcUrl: String = config.baseUrl("mtd-tax-calculation")
  def propertyUrl: String = config.baseUrl("mtd-property-api")
  def selfEmploymentUrl: String = config.baseUrl("mtd-self-employment")
  def release2Enabled: Boolean = config.getBoolean("release-2.enabled")
  def dividendsApiUrl: String = config.baseUrl("mtd-dividends-income")
  def savingsAccountApiUrl: String = config.baseUrl("mtd-savings-accounts")
  def crystallisationApiUrl: String = config.baseUrl("mtd-crystallisation")

}
