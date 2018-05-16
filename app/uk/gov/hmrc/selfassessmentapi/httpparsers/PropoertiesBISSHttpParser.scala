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

package uk.gov.hmrc.selfassessmentapi.httpparsers

import play.api.Logger
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertiesBISS
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Error {
  val INVALID_NINO = "NINO_INVALID"
  val INVALID_TAX_YEAR = "TAX_YEAR_INVALID"

  val reads: Reads[(String, String)] = (
    (__ \ "code").read[String] and
      (__ \ "message").read[String]
  )((c, m) => c -> m)

  def unapply(arg: JsValue): Option[(String, String)] = {
    reads.reads(arg).fold(_ => None, valid => Some(valid))
  }
}

sealed trait DesError

case object InvalidNino extends DesError
case object InvalidTaxYear extends DesError

trait PropertiesBISSHttpParser {
  import Error._

  type PropertiesBISSOutcome = Either[DesError, PropertiesBISS]

  implicit val propertiesBISSHttpParser = new HttpReads[PropertiesBISSOutcome] {
    override def read(method: String, url: String, response: HttpResponse): PropertiesBISSOutcome = {
      (response.status, response.json) match {
        case (OK, _) => response.json.validate[PropertiesBISS].fold(
          invalid => {
            Logger.warn(s"[PropertiesBISSHttpParser] - Error reading NRS Response: $invalid")
            Left(error)
          },
          valid => Right(valid)
        )
        case (BAD_REQUEST, Error(INVALID_NINO, msg)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - $msg")
          Left(InvalidNino)
        }
        case (BAD_REQUEST, Error(INVALID_TAX_YEAR, msg)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - $msg")
          Left(InvalidTaxYear)
        }
        case (NOT_FOUND, _) => Left(error)
        case (status, _) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Non-OK NRS Response: STATUS $status")
          Left(error)
      }
    }
  }
}

sealed trait PropertiesBISSError extends DesError

case object error extends PropertiesBISSError

case object NoSubmissionDataExists extends PropertiesBISSError
