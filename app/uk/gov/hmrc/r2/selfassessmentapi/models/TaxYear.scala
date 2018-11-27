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

package uk.gov.hmrc.r2.selfassessmentapi.models

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain.{SimpleName, SimpleObjectReads, SimpleObjectWrites}

case class TaxYear(taxYear: String) extends SimpleName {

  private lazy val year = taxYear.split("-")(0).toInt
  override def toString = taxYear
  def value = taxYear
  def toDesTaxYear = taxYear.take(2) + taxYear.drop(5)
  val name = "taxYear"
  val taxYearFromDate = new LocalDate(s"$year-04-06")
  val taxYearToDate = new LocalDate(s"${year+1}-04-05")
}

object TaxYear {
  implicit val taxYearWrite: Writes[TaxYear] = new SimpleObjectWrites[TaxYear](_.value)
  implicit val taxYearRead: Reads[TaxYear] = new SimpleObjectReads[TaxYear]("taxYear", TaxYear.apply)

  val taxYearFormat = "20[1-9][0-9]\\-[1-9][0-9]"

  def createTaxYear(taxYear: String): Option[TaxYear] = {
    for {
      a <- hasValidFormat(taxYear)
      b <- isAfterYear(2016, a)
    } yield TaxYear(b)
  }

  private def hasValidFormat(taxYear: String): Option[String] = {
    taxYear match {
      case x if x.matches(taxYearFormat) => Some(taxYear)
      case _ => None
    }
  }

  private def isAfterYear(year: Int, taxYear: String): Option[String] = {
    taxYear.split("-") match {
      case Array(f, s) => if (f.toInt > year && (s.toInt + 2000) == f.toInt + 1) Some(taxYear) else None
    }
  }
}
