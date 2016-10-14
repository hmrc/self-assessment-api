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

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.DividendIncomeType.DividendIncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson._

object DividendIncomeType extends Enumeration {
  type DividendIncomeType = Value
  val FromUKCompanies, FromOtherUKSources = Value
  implicit val format = enumFormat(DividendIncomeType, Some("Dividend income type is invalid"))
}

case class DividendIncome(id: Option[String] = None, `type`: DividendIncomeType, amount: BigDecimal)

object DividendIncome extends JsonMarshaller[DividendIncome] {

  implicit val writes = Json.writes[DividendIncome]

  implicit val reads: Reads[DividendIncome] = (
    Reads.pure(None) and
      (__ \ "type").read[DividendIncomeType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (DividendIncome.apply _)

  override def example(id: Option[SummaryId]) = DividendIncome(id, DividendIncomeType.FromUKCompanies, BigDecimal(1000.00))
}
