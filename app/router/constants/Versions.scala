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

package router.constants

import play.api.http.HeaderNames.ACCEPT
import play.api.mvc.{AnyContent, Request, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier

object Versions {
  val VERSION_1 = "1.0"
  val VERSION_2 = "2.0"

  private val versionRegex = """application\/vnd.hmrc.(\d.\d)\+json""".r

  def getFromRequest(request: RequestHeader): Option[String] =
    getFrom(request.headers.headers)

  private def getFrom(headers: Seq[(String, String)]): Option[String] =
    headers.collectFirst { case (ACCEPT, versionRegex(ver)) => ver }

  def getAPIVersionFromRequest(implicit request: Request[AnyContent]): Option[String] = {
    extractAcceptHeader(request).map(header => header.version)
  }

  def getAPIVersionFromRequest(implicit request: RequestHeader): Option[String] = {
    extractAcceptHeader(request).map(header => header.version)
  }

  def extractAcceptHeader[A](req: RequestHeader): Option[AcceptHeader] = {
    val versionRegex = """^application/vnd\.hmrc\.(\d\.\d)\+(.*)$""".r
    req.headers.get(ACCEPT).flatMap {
      case versionRegex(version, contentType) => Some(AcceptHeader(version, contentType))
      case _ => None
    }
  }

  def extractAcceptHeader[A](req: Request[A]): Option[AcceptHeader] = {
    val versionRegex = """^application/vnd\.hmrc\.(\d\.\d)\+(.*)$""".r
    req.headers.get(ACCEPT).flatMap {
      case versionRegex(version, contentType) => Some(AcceptHeader(version, contentType))
      case _ => None
    }
  }

  def convertHeaderToVersion1(implicit hc: HeaderCarrier): HeaderCarrier = {
    val convertAcceptHeader: PartialFunction[(String, String), (String, String)] = {
      case (ACCEPT, _) => (ACCEPT, "application/vnd.hmrc.1.0+json")
      case header => header
    }
    hc.copy(otherHeaders = hc.otherHeaders.map(convertAcceptHeader))
  }

  case class AcceptHeader(version: String, contentType: String)
}
