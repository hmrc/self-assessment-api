/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.config

import play.api.Play._
import uk.gov.hmrc.play.config.ServicesConfig

object AppContext extends ServicesConfig {
  lazy val appName = current.configuration.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  lazy val appUrl = current.configuration.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  lazy val apiGatewayContext = current.configuration.getString("api.gateway.context")
  lazy val apiGatewayRegistrationContext = apiGatewayContext.getOrElse(throw new RuntimeException("api.gateway.context is not configured"))
  lazy val apiGatewayLinkContext = apiGatewayContext.map(x => if(x.isEmpty) x else s"/$x").getOrElse("")
  lazy val apiStatus = current.configuration.getString("api.status").getOrElse(throw new RuntimeException("api.status is not configured"))
  lazy val serviceLocatorUrl: String = baseUrl("service-locator")
  lazy val authUrl: String = baseUrl("auth")
  lazy val desUrl: String = baseUrl("des")
  lazy val registrationEnabled: Boolean = current.configuration.getBoolean(s"$env.microservice.services.service-locator.enabled").getOrElse(true)
  lazy val featureSwitch = current.configuration.getConfig(s"$env.feature-switch")
  lazy val updateTaxYearPropertiesEnabled: Boolean = current.configuration.getBoolean(s"update-tax-year-properties.enabled").getOrElse(true)
  lazy val authEnabled: Boolean = current.configuration.getBoolean(s"auth.enabled").getOrElse(true)
  lazy val auditEnabled: Boolean = current.configuration.getBoolean(s"audit.enabled").getOrElse(true)
  def deleteExpiredDataJob = current.configuration.getConfig(s"$env.scheduling.deleteExpiredDataJob").getOrElse(throw new RuntimeException(s"$env.scheduling.deleteExpiredDataJob is not configured"))
  def dropMongoCollectionJob = current.configuration.getConfig(s"$env.scheduling.dropMongoCollectionJob").getOrElse(throw new RuntimeException(s"$env.scheduling.dropMongoCollectionJob is not configured"))

  val supportedTaxYears: Set[String] = Set("2016-17")
}



