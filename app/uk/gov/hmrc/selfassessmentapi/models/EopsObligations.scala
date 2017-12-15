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

import com.github.nscala_time.time.OrderingImplicits
import org.joda.time.LocalDate
import play.api.libs.json.{Json, Writes}

import scala.util.{Failure, Success, Try}

case class EopsObligations(id: Option[SourceId] = None, obligations: Seq[EopsObligation])

object EopsObligations {
  implicit val writes: Writes[EopsObligations] = Json.writes[EopsObligations]
}

case class EopsObligation(start: LocalDate, end: LocalDate, due: LocalDate, finalised: Boolean)

object EopsObligation {

  implicit val from =  new DesTransformValidator[des.ObligationDetail, EopsObligation] {
    def from(desObligation: des.ObligationDetail) = {
      Try(EopsObligation(
        start = LocalDate.parse(desObligation.inboundCorrespondenceFromDate),
        end = LocalDate.parse(desObligation.inboundCorrespondenceToDate),
        due = LocalDate.parse(desObligation.inboundCorrespondenceDueDate),
        finalised = desObligation.isFinalised)
      ) match {
        case Success(obj) => Right(obj)
        case Failure(ex) => Left(InvalidDateError(s"Unable to parse the date from des response $ex"))
      }
    }
  }

  implicit val localDateOrder: Ordering[LocalDate] = OrderingImplicits.LocalDateOrdering
  implicit val ordering: Ordering[EopsObligation] = Ordering.by(_.start)

  implicit val writes: Writes[EopsObligation] = Json.writes[EopsObligation]
}
