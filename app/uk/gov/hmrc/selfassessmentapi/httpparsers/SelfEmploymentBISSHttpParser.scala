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
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentBISS

object SelfEmploymentBISSHttpParser {
  type SelfEmploymentBISSOutcome = Either[ErrorWrapper, SelfEmploymentBISS]
}

trait SelfEmploymentBISSHttpParser extends HttpParser {
  import SelfEmploymentBISSHttpParser._

  implicit val selfEmploymentBISSHttpParser = new HttpReads[SelfEmploymentBISSOutcome] {
    override def read(method: String, url: String, response: HttpResponse): SelfEmploymentBISSOutcome = {
      (response.status, response.jsonOpt) match {
        case (OK, _) => response.jsonOpt.validate[SelfEmploymentBISS].fold(
          invalid => {
            Logger.warn(s"[selfEmploymentBISSHttpParser] - Error reading DES Response: $invalid")
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
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TAX_YEAR)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid tax year")
          Left(ErrorWrapper(TaxYearInvalid, None))
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_INCOMESOURCEID)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid Nino")
          Left(ErrorWrapper(NinoInvalid, None))
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_INCOMESOURCETYPE)) =>
          Logger.warn(s"[PropertiesBISSHttpParser] - Invalid income source type")
          Left(ErrorWrapper(ServerError, None))
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
