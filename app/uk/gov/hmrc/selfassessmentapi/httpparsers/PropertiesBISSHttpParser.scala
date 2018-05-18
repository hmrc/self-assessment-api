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
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertiesBISS
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode

object PropertiesBISSHttpParser {
  type PropertiesBISSOutcome = Either[Error, PropertiesBISS]

  val NO_DATA_EXISTS = "NO_DATA_EXISTS"
}

trait PropertiesBISSHttpParser extends HttpParser {
  import PropertiesBISSHttpParser._

  implicit val propertiesBISSHttpParser = new HttpReads[PropertiesBISSOutcome] {
    override def read(method: String, url: String, response: HttpResponse): PropertiesBISSOutcome = {
      (response.status, response.jsonOpt) match {
        case (OK, _) => response.jsonOpt.validate[PropertiesBISS].fold(
          invalid => {
            Logger.warn(s"[PropertiesBISSHttpParser] - Error reading DES Response: $invalid")
            Left(ServerError)
          },
          valid => Right(valid)
        )
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_NINO)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid Nino")
          Left(NinoInvalid)
        }
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TAX_YEAR)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid tax year")
          Left(TaxYearInvalid)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND_NINO)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - Nino not found")
          Left(NinoNotFound)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND_TAX_YEAR)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - Tax year not found")
          Left(TaxYearNotFound)
        }
        case (NOT_FOUND, ErrorCode(DesErrorCode.NO_DATA_EXISTS)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - No submissions data exists for provided tax year")
          Left(NoSubmissionDataExists)
        }
        case (INTERNAL_SERVER_ERROR, ErrorCode(DesErrorCode.SERVER_ERROR)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - An error has occurred with DES")
          Left(ServerError)
        }
        case (SERVICE_UNAVAILABLE, ErrorCode(DesErrorCode.SERVICE_UNAVAILABLE)) => {
          Logger.warn(s"[PropertiesBISSHttpParser] - DES is currently down")
          Left(ServiceUnavailable)
        }
        case (status, _) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Non-OK DES Response: STATUS $status")
          Left(ServerError)
      }
    }
  }
}

object NoSubmissionDataExists extends Error("NO_DATA_EXISTS", "No submissions data exists for provided tax year", None)
