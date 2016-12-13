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

package uk.gov.hmrc.selfassessmentapi.resources.models

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain.{SimpleName, SimpleObjectReads, SimpleObjectWrites}

case class TaxYear(taxYear: String) extends SimpleName {

  override def toString = taxYear
  def value = taxYear
  val name = "taxYear"
}

object TaxYear extends (String => TaxYear) {
  implicit val taxYearWrite: Writes[TaxYear] = new SimpleObjectWrites[TaxYear](_.value)
  implicit val taxYearRead: Reads[TaxYear] = new SimpleObjectReads[TaxYear]("taxYear", TaxYear.apply)
}
