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

import play.api.Configuration
import uk.gov.hmrc.selfassessmentapi.config.AppContext._
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYearPropertyType
import uk.gov.hmrc.selfassessmentapi.resources.models.SourceType.SourceType

case class FeatureSwitch(value: Option[Configuration]) {
  val DEFAULT_VALUE = true

  def isEnabled(sourceType: SourceType): Boolean = value match {
    case Some(config) => FeatureConfig(config).isSourceEnabled(sourceType.toString)
    case None => DEFAULT_VALUE
  }

  def isEnabled(sourceType: SourceType, summary: String): Boolean = value match {
    case Some(config) =>
      if(summary.isEmpty) FeatureConfig(config).isSourceEnabled(sourceType.toString)
      else FeatureConfig(config).isSummaryEnabled(sourceType.toString, summary)
    case None => DEFAULT_VALUE
  }

  def isEnabled(source: TaxYearPropertyType): Boolean = value match {
    case Some(config) => FeatureConfig(config).isSourceEnabled(source.name)
    case None => DEFAULT_VALUE
  }

  def isWhiteListingEnabled: Boolean = {
    value match {
      case Some(config) => config.getBoolean("white-list.enabled").getOrElse(false)
      case None => false
    }
  }

  def whiteListedApplicationIds: Seq[String] = {
    value match {
      case Some(config) => config.getStringSeq("white-list.applicationIds").getOrElse(throw new RuntimeException(s"$env.feature-switch.white-list.applicationIds is not configured"))
      case None => Seq()
    }
  }
}

sealed case class FeatureConfig(config: Configuration) {

  def isSummaryEnabled(source: String, summary: String): Boolean = {
    val summaryEnabled = config.getBoolean(s"$source.$summary.enabled") match {
      case Some(flag) => flag
      case None => true
    }
    isSourceEnabled(source) && summaryEnabled
  }

  def isSourceEnabled(source: String): Boolean = {
    config.getBoolean(s"$source.enabled").getOrElse(true)
  }
}
