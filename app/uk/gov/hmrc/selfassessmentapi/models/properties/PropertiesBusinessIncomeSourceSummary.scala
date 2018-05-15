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

package uk.gov.hmrc.selfassessmentapi.models.properties

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.selfassessmentapi.models.Amount

case class PropertiesBusinessIncomeSourceSummary(totalIncome: Amount,
                                                 totalExpenses: Amount,
                                                 netProfit: Amount,
                                                 netLoss: Amount,
                                                 totalAdditions: Option[Amount],
                                                 totalDeductions: Option[Amount],
                                                 taxableProfit: Amount,
                                                 taxableLoss: Amount)

object PropertiesBusinessIncomeSourceSummary{
  implicit val format: Format[PropertiesBusinessIncomeSourceSummary] = Json.format[PropertiesBusinessIncomeSourceSummary]

  val desFormat: Format[PropertiesBusinessIncomeSourceSummary] = Json.format[PropertiesBusinessIncomeSourceSummary]
}
