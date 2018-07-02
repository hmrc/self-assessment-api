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

package router.services

import play.api.Logger
import play.api.http.HeaderNames.ACCEPT
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait Service {

  private[services] def withApiVersion(pf: PartialFunction[Option[String], Future[SelfAssessmentOutcome]])
                                      (implicit hc: HeaderCarrier): Future[SelfAssessmentOutcome] = {
    pf.orElse[Option[String], Future[SelfAssessmentOutcome]]{
      case Some(_) =>
        Logger.info("request header contains an unsupported api version")
        Future.successful(Left(UnsupportedAPIVersion))
      case None =>
        Logger.info("request header contains an incorrect or empty api version")
        Future.successful(Left(IncorrectAPIVersion))
    }(getAPIVersionFromRequest)
  }

  private[services] def getAPIVersionFromRequest(implicit hc: HeaderCarrier): Option[String] = {
    val versionRegex = """application\/vnd.hmrc.(\d.\d)\+json""".r
    hc.headers.collectFirst{ case (ACCEPT, versionRegex(ver)) => ver }
  }

  private [services] def convertHeaderToVersion1(implicit hc: HeaderCarrier) = {
    val convertAcceptHeader: PartialFunction[(String, String), (String, String)] = {
      case (ACCEPT, _) => (ACCEPT, "application/vnd.hmrc.1.0+json")
      case header =>  header
    }
    hc.copy(otherHeaders = hc.otherHeaders.map(convertAcceptHeader))
  }
}
