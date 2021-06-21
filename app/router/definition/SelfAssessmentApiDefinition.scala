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

package router.definition

import config.AppConfig
import router.constants.Versions._
import router.definition.APIStatus.APIStatus
import uk.gov.hmrc.auth.core.ConfidenceLevel
import utils.Logging

import javax.inject.{Inject, Singleton}

@Singleton
class SelfAssessmentApiDefinition @Inject()(appConfig: AppConfig) extends Logging {

  private val readScope = "read:self-assessment"
  private val writeScope = "write:self-assessment"

  def confidenceLevel: ConfidenceLevel = if (appConfig.confidenceLevelDefinitionConfig) ConfidenceLevel.L200 else ConfidenceLevel.L50

  lazy val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "View your Self Assessment information",
          description = "Allow read access to self assessment data",
          confidenceLevel = confidenceLevel
        ),
        Scope(
          key = writeScope,
          name = "Change your Self Assessment information",
          description = "Allow write access to self assessment data",
          confidenceLevel = confidenceLevel
        )
      ),
      api = APIDefinition(
        name = "Self Assessment (MTD)",
        description = "An API for providing self assessment data and obtaining tax calculations",
        context = appConfig.apiGatewayContext,
        versions = Seq(
          APIVersion(
            version = VERSION_1,
            status = buildAPIStatus(VERSION_1),
            endpointsEnabled = true),
          APIVersion(
            version = VERSION_2,
            status = buildAPIStatus(VERSION_2),
            endpointsEnabled = true)
        ),
        requiresTrust = None
      )
    )

  private[definition] def buildAPIStatus(version: String): APIStatus = {
    appConfig.apiStatus(version) match {
      case "ALPHA" => APIStatus.ALPHA
      case "BETA" => APIStatus.BETA
      case "STABLE" => APIStatus.STABLE
      case "DEPRECATED" => APIStatus.DEPRECATED
      case "RETIRED" => APIStatus.RETIRED
      case _ => logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
    }
  }
}
