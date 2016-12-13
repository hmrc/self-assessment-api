/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.kenshoo.monitoring

import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MonitoredWSHttp extends WSHttp with HttpAPIMonitor {

  private case class HttpAPI(urlPattern: String, name: String)

  private case class HttpAPINames(urlPatternAndName: Map[String, String]) {
    val httpAPIs = urlPatternAndName.map { httpApi => HttpAPI(httpApi._1, httpApi._2) }

    def nameFor(method: String, url: String) = {
      httpAPIs.find(downstreamService => url.matches(downstreamService.urlPattern)) match {
        case Some(service) => Some(s"ConsumedAPI-${service.name}-$method")
        case None => None
      }
    }
  }

  val httpAPIs: Map[String, String]
  private lazy val apiNames = HttpAPINames(httpAPIs)

  def monitorRequestsWithoutBodyIfUrlPatternIsKnown(method: String, url: String)(func: => Future[HttpResponse])(implicit hc: HeaderCarrier) = {
    apiNames.nameFor(method, url) match {
      case None =>
        Logger.debug(s"ConsumedAPI-Not-Monitored: $method-$url")
        func
      case Some(name) =>
        monitor(name) { func }
    }
  }

  def monitorRequestsWithBodyIfUrlPatternIsKnown[A : Writes](method: String, url: String)(func: => Future[HttpResponse])(implicit hc: HeaderCarrier) = {
    apiNames.nameFor(method, url) match {
      case None =>
        Logger.debug(s"ConsumedAPI-Not-Monitored: $method-$url")
        func
      case Some(name) =>
        monitor(name) { func }
    }
  }

  override def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    monitorRequestsWithoutBodyIfUrlPatternIsKnown("GET", url) {
      super.doGet(url)
    }
  }

  override def doPost[A : Writes](url: String, body: A, headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    monitorRequestsWithBodyIfUrlPatternIsKnown("POST", url) {
      super.doPost(url, body, headers)
    }
  }

  override def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    monitorRequestsWithoutBodyIfUrlPatternIsKnown("POST", url) {
      super.doEmptyPost(url)
    }
  }

  override def doPut[A : Writes](url: String, body: A)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    monitorRequestsWithBodyIfUrlPatternIsKnown("PUT", url) {
      super.doPut(url, body)
    }
  }

  override def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    monitorRequestsWithoutBodyIfUrlPatternIsKnown("DELETE", url) {
      super.doDelete(url)
    }
  }
}
