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

package mocks.config

import config.AppConfig
import config.AppConfig.RequestMethodAndRoute
import mocks.Mock
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import play.api.Configuration

trait MockAppConfig extends Mock { _: Suite =>

  val mockAppConfig = mock[AppConfig]

  object MockAppConfig {
    def appName: OngoingStubbing[String] = when(mockAppConfig.appName)
    def appUrl: OngoingStubbing[String] = when(mockAppConfig.appUrl)
    def apiStatus(version: String): OngoingStubbing[String] = when(mockAppConfig.apiStatus(any[String]()))
    def featureSwitch: OngoingStubbing[Option[Configuration]] = when(mockAppConfig.featureSwitch)
    def registrationEnabled: OngoingStubbing[Boolean] = when(mockAppConfig.registrationEnabled)

    def saApiUrl: OngoingStubbing[String] = when(mockAppConfig.saApiUrl)
    def cgApiUrl: OngoingStubbing[String] = when(mockAppConfig.cgApiUrl)
    def taxCalcUrl: OngoingStubbing[String] = when(mockAppConfig.taxCalcUrl)
    def propertyUrl: OngoingStubbing[String] =  when(mockAppConfig.propertyUrl)
    def selfEmploymentUrl: OngoingStubbing[String] =  when(mockAppConfig.selfEmploymentUrl)
    def dividendsApiUrl: OngoingStubbing[String] = when(mockAppConfig.dividendsApiUrl)
    def savingsAccountsApiUrl: OngoingStubbing[String] = when(mockAppConfig.savingsAccountApiUrl)
    def crystallisationApiUrl: OngoingStubbing[String] = when(mockAppConfig.crystallisationApiUrl)

    def deprecatedRoutes[A]: OngoingStubbing[Seq[RequestMethodAndRoute]] = when(mockAppConfig.deprecatedRoutes)

    def environmentHeaders: OngoingStubbing[Option[Seq[String]]] = when(mockAppConfig.environmentHeaders)

    def confidenceLevelDefinitionConfig: OngoingStubbing[Boolean] = when(mockAppConfig.confidenceLevelDefinitionConfig)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppConfig)
  }
}
