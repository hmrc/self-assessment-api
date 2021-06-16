/*
 * Copyright 2021 HM Revenue & Customs
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

package router.httpParsers

import router.errors.SelfAssessmentAPIError
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging

class SelfAssessmentHttpParser extends HttpReads[SelfAssessmentOutcome] with Logging {

  override def read(method: String, url: String, response: HttpResponse): SelfAssessmentOutcome = {
    logger.debug(s"[SelfAssessmentConnector] [get] - Success: ${response.body}")
    Right(response)
  }
}

object SelfAssessmentHttpParser {
  type SelfAssessmentOutcome = Either[SelfAssessmentAPIError, HttpResponse]
}
