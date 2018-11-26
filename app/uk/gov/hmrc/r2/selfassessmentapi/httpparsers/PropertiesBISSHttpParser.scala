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

package uk.gov.hmrc.r2.selfassessmentapi.httpparsers

import play.api.Logger
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.r2.selfassessmentapi.models.Errors.{NinoInvalid, NoSubmissionDataExists, ServerError, ServiceUnavailable, _}
import uk.gov.hmrc.r2.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertiesBISS

object PropertiesBISSHttpParser {
  type PropertiesBISSOutcome = Either[ErrorWrapper, PropertiesBISS]

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
            Left(ErrorWrapper(ServerError, None))
          },
          valid => Right(valid)
        )
        case (BAD_REQUEST, MultipleErrorCode()) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Multiple errors")
          Left(parseErrors(response.jsonOpt.get))
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_IDVALUE)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid Nino")
          Left(ErrorWrapper(NinoInvalid, None))
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_IDTYPE)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid ID TYPE")
          Left(ErrorWrapper(ServerError, None))
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TAX_YEAR)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid tax year")
          Left(ErrorWrapper(TaxYearInvalid, None))
        case (NOT_FOUND, ErrorCode(DesErrorCode.NOT_FOUND)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - No submissions data exists for provided tax year")
          Left(ErrorWrapper(NoSubmissionDataExists, None))
        case (INTERNAL_SERVER_ERROR, ErrorCode(DesErrorCode.SERVER_ERROR)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - An error has occurred with DES")
          Left(ErrorWrapper(ServerError, None))
        case (SERVICE_UNAVAILABLE, ErrorCode(DesErrorCode.SERVICE_UNAVAILABLE)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - DES is currently down")
          Left(ErrorWrapper(ServiceUnavailable, None))
        case (status, _) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Non-OK DES Response: STATUS $status")
          Left(ErrorWrapper(ServerError, None))
      }
    }
  }
}

