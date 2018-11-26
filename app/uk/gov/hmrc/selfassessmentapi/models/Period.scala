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

package uk.gov.hmrc.selfassessmentapi.models

import org.joda.time.LocalDate

import scala.util.{Failure, Success, Try}

trait Period {

  val from: LocalDate
  val to: LocalDate

  def periodId: String = Period.id(from, to)

  def valid = from.equals(to) || from.isBefore(to)

}

object Period {

  val periodPattern = """(\d{4}-\d{2}-\d{2})_(\d{4}-\d{2}-\d{2})""".r

  def id(f: LocalDate, t: LocalDate): String = s"${f}_$t"

  def apply(f: LocalDate, t:LocalDate) = new Period {
    override val from: LocalDate = f
    override val to: LocalDate = t
  }

  def unapply(period: String): Option[(LocalDate, LocalDate)] = {
    period match {
      case periodPattern(from, to) =>
        Try(Some((LocalDate.parse(from), LocalDate.parse(to)))) match {
          case Success(x) => x
          case Failure(_) => None
        }
      case _ => None
    }
  }

}
