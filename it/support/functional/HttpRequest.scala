/*
 * Copyright 2020 HM Revenue & Customs
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

package support.functional

import play.api.libs.ws.{EmptyBody, WSRequest, WSResponse}
import support.WSClient

import scala.concurrent.Future

trait HttpRequest extends WSClient {

  private val GET = "GET"
  private val POST = "POST"
  private val PUT = "PUT"

  case class HttpRequest(verb: String, wsRequest: WSRequest, body: Option[String] = None) {
    def execute: Future[WSResponse] = body.fold(executeNoBody)(executeWithBody)

    private def executeNoBody: Future[WSResponse] = verb match {
      case GET  => wsRequest.get()
      case POST => wsRequest.post(EmptyBody)
      case PUT => wsRequest.put(EmptyBody)
    }

    private def executeWithBody(body: String): Future[WSResponse] = verb match {
      case GET  => wsRequest.get()
      case POST => wsRequest.post(body)
      case PUT => wsRequest.put(body)
    }
  }

  def httpGet(url: String): HttpRequest = {
    HttpRequest(GET, buildRequest(url))
  }

  def httpPost(url: String): HttpRequest = {
    HttpRequest(POST, buildRequest(url))
  }

  def httpPut(url: String): HttpRequest = {
    HttpRequest(PUT, buildRequest(url))
  }

  def requestWithHeaders(request: HttpRequest, headers: Seq[(String, String)]): HttpRequest = {
    request.copy(wsRequest = request.wsRequest.withHttpHeaders(headers: _*))
  }

  def requestWithBody(request: HttpRequest, body: String): HttpRequest = {
    request.copy(body = Some(body))
  }
}
