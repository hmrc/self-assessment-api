/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.support

import play.api.Play.current
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import play.api.mvc.Results
import uk.gov.hmrc.play.http.ws.WSHttpResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Http {

  def get(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.get()
  }

  def post[A](url: String, body: A, headers: Seq[(String, String)] = Seq.empty)(
      implicit writes: Writes[A],
      hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.withFollowRedirects(false).post(Json.toJson(body))
  }

  def postJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.withFollowRedirects(false).post(body)
  }

  def putJson(url: String, body: JsValue, headers: Seq[(String, String)] = Seq.empty)(
      implicit hc: HeaderCarrier,
      timeout: FiniteDuration): HttpResponse = perform(url) { request =>
    request.put(body)
  }

  def postEmpty(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
    request =>
      request.post(Results.EmptyContent())
  }

  def delete(url: String)(implicit hc: HeaderCarrier, timeout: FiniteDuration): HttpResponse = perform(url) {
    request =>
      request.delete()
  }

  private def perform(url: String)(fun: WSRequest => Future[WSResponse])(implicit hc: HeaderCarrier,
                                                                         timeout: FiniteDuration): WSHttpResponse =
    await(fun(WS.url(url).withHeaders(hc.headers: _*).withRequestTimeout(timeout)).map(new WSHttpResponse(_)))

  private def await[A](future: Future[A])(implicit timeout: FiniteDuration) = Await.result(future, timeout)

}
