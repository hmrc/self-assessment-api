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

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PropertiesBISS(totalIncome: BigDecimal,
                                                 totalExpenses: BigDecimal,
                                                 netProfit: BigDecimal,
                                                 netLoss: BigDecimal,
                                                 totalAdditions: Option[BigDecimal],
                                                 totalDeductions: Option[BigDecimal],
                                                 taxableProfit: BigDecimal,
                                                 taxableLoss: BigDecimal)

object PropertiesBISS{
  implicit val format: OFormat[PropertiesBISS] = Json.format[PropertiesBISS]

  val desWrites: OWrites[PropertiesBISS] = (
    (__ \ "totalIncome").write[BigDecimal] and
      (__ \ "totalExpenses").write[BigDecimal] and
      (__ \ "netProfit").write[BigDecimal] and
      (__ \ "netLoss").write[BigDecimal] and
      (__ \ "totalAdditions").writeNullable[BigDecimal] and
      (__ \ "totalDeductions").writeNullable[BigDecimal] and
      (__ \ "taxableProfit").write[BigDecimal] and
      (__ \ "taxableLoss").write[BigDecimal]
    )(PropertiesBISS.apply _)

  val desReads: Reads[PropertiesBISS] = (
    (__ \ "totalIncome").read[BigDecimal] and
      (__ \ "totalExpenses").read[BigDecimal] and
      (__ \ "netProfit").read[BigDecimal] and
      (__ \ "netLoss").read[BigDecimal] and
      (__ \ "totalAdditions").readNullable[BigDecimal] and
      (__ \ "totalDeductions").readNullable[BigDecimal] and
      (__ \ "taxableProfit").read[BigDecimal] and
      (__ \ "taxableLoss").read[BigDecimal]
    )(PropertiesBISS.apply _)
}
