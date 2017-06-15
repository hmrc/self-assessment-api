/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models

package object des {
  def expense2Deduction(expense: models.Expense): Deduction =
    Deduction(amount = expense.amount, disallowableAmount = expense.disallowableAmount)

  implicit val bigIntFormat = new Format[BigInt] {
    override def reads(json: JsValue): JsResult[BigInt] = json match {
      case JsNumber(n) => n.toBigIntExact().map(JsSuccess(_)).getOrElse(JsError("Value is not an integer"))
      case _           => JsError("Value is not an integer")
    }

    override def writes(o: BigInt): JsValue = JsNumber(BigDecimal(o))
  }
}
