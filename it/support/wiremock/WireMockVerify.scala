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

package support.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest.Assertion
import org.scalatest.Matchers._

import scala.collection.JavaConversions._

trait WireMockVerify {

  private def allMocks: List[LoggedRequest] = WireMock.findAll(RequestPatternBuilder.allRequests()).toList

  def getMockFor(url: String): LoggedRequest = {
    // use equals as well as regex matching to account for query parameters separators not being able to be passed
    val mock: Option[LoggedRequest] = allMocks.find(y=> y.getUrl.equalsIgnoreCase(url) || y.getUrl.matches(url))
    require(mock.isDefined, s"Trying to verify WireMock stubbing but none found for $url")
    mock.get
  }

  def verifyHeaderExists(mock: LoggedRequest, header: (String, String)): Assertion = {
    val (name, value) = header
    withClue(s"Trying to verify mock for ${mock.getUrl} has header $name, but does not\n") {
      mock.containsHeader(name) shouldBe true
    }
    val mockHeader = mock.header(name)
    withClue(s"Trying to verify mock for ${mock.getUrl} has header $name with value $value, but does not in ${getValues(mockHeader)}\n"){
      mockHeader.containsValue(value) shouldBe true
    }
  }

  def getValues(header: HttpHeader): List[String] = {
    if(header.isPresent) header.values().toList else List.empty
  }
}
