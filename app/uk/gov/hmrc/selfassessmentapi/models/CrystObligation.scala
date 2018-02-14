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
import play.api.libs.json.{Json, Writes}

import scala.util.{Failure, Success, Try}

case class CrystObligation(start: LocalDate, end: LocalDate, due: Option[LocalDate] = None, processed: Option[LocalDate] = None, met: Boolean)

object toObligation {

  def apply(desObligation: des.ObligationDetail): Either[DesTransformError, CrystObligation] =
    Try {
      val start = LocalDate.parse(desObligation.inboundCorrespondenceFromDate)
      val end = LocalDate.parse(desObligation.inboundCorrespondenceToDate)
      val met = desObligation.isFinalised
      if (met) {
        CrystObligation(start = start, end = end,
          processed = Some(LocalDate.parse(desObligation.inboundCorrespondenceDateReceived.get)),
          met = met)
      } else {
        CrystObligation(start = start, end = end,
          due = Some(LocalDate.parse(desObligation.inboundCorrespondenceDueDate)),
          met = met)
      }

    } match {
      case Success(obj) => Right(obj)
      case Failure(ex) => Left(InvalidDateError(s"Unable to parse the date from des response $ex"))
    }
}

object CrystObligation {
  implicit val writes: Writes[CrystObligation] = Json.writes[CrystObligation]
}
