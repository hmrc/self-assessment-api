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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SelfEmploymentBISS(total: Total,
                              accountingAdjustments: Option[BigDecimal] = None,
                              profit: Profit,
                              loss: Loss)

object SelfEmploymentBISS {
  implicit val writes: OWrites[SelfEmploymentBISS] = Json.writes[SelfEmploymentBISS]

  implicit val desReads: Reads[SelfEmploymentBISS] = (
    (__ \ "totalIncome").read[BigDecimal] and
      (__ \ "totalExpenses").read[BigDecimal] and
      (__ \ "totalAdditions").readNullable[BigDecimal] and
      (__ \ "totalDeductions").readNullable[BigDecimal] and
      (__ \ "accountingAdjustments").readNullable[BigDecimal] and
      (__ \ "netProfit").read[BigDecimal] and
      (__ \ "taxableProfit").read[BigDecimal] and
      (__ \ "netLoss").read[BigDecimal] and
      (__ \ "taxableLoss").read[BigDecimal]
    ){
      (income, expenses, additions, deductions, accountingAdjustments, netProfit, taxableProfit, netLoss, taxableLoss) =>
        SelfEmploymentBISS(
          Total(income, expenses, additions, deductions),
          accountingAdjustments,
          Profit(netProfit, taxableProfit),
          Loss(netLoss, taxableLoss)
        )
    }
}

case class Total(income: BigDecimal,
                 expenses: BigDecimal,
                 additions: Option[BigDecimal],
                 deductions: Option[BigDecimal])

object Total {
  implicit val write: OWrites[Total] = Json.writes[Total]
}

case class Profit(net: BigDecimal,
                  taxable: BigDecimal)

object Profit {
  implicit val write: OWrites[Profit] = Json.writes[Profit]
}

case class Loss(net: BigDecimal,
                  taxable: BigDecimal)

object Loss {
  implicit val write: OWrites[Loss] = Json.writes[Loss]
}