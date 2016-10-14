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

package uk.gov.hmrc.selfassessmentapi.controllers.api.dividend

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.SummaryTypes.Incomes

object SourceType {

  case object Dividends extends SourceType {
    override val name = "dividends"
    override val documentationName = "Dividends"
    override val summaryTypes: Set[SummaryType] = Set(Incomes)
    override val title = "Sample dividends"

    override def description(action: String) = s"$action a dividend income"
    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(Dividend.example(sourceId))
  }

}
