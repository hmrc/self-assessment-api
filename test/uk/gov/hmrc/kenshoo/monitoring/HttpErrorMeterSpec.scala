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

import com.codahale.metrics.Meter
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify}
import uk.gov.hmrc.play.http.{HeaderCarrier, _}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpErrorMeterSpec extends UnitSpec {

  implicit val hc = HeaderCarrier()

  "2xx response" should {
    "not be counted" in new HttpErrorRateMeterTest {
      List(200, 201, 204).foreach { status =>
        await(countErrors("servicename") {
          Future.successful(HttpResponse(status))
        })
        verify(registry, never()).getTimers
      }
    }
  }

  "3xx response" should {
    "not be counted" in new HttpErrorRateMeterTest {
      List(301, 302, 303).foreach { status =>
        await(countErrors("servicename") {
          Future.successful(HttpResponse(status))
        })
        verify(registry, never()).getTimers
      }
    }
  }

  "4xx response" should {
    "be counted" in new HttpErrorRateMeterTest {
      List(400, 401, 404).foreach { status =>
        await(countErrors("servicename") {
          Future.successful(HttpResponse(status))
        })
      }
      verify(errorMeter4xx, Mockito.times(3)).mark()
    }
  }

  "Upstream4xxResponse" should {
    "be counted" in new HttpErrorRateMeterTest {
      List(400, 401, 404).foreach { status =>
        try {
          await(countErrors("servicename") {
            Future.failed(Upstream4xxResponse("foobar", status, status))
          })
        } catch {
          case ex: Throwable => ()
        }
      }
      verify(errorMeter4xx, Mockito.times(3)).mark()
    }
  }

  "5xx response" should {
    "be counted" in new HttpErrorRateMeterTest {
      List(500, 503).foreach { status =>
        await(countErrors("servicename") {
          Future.successful(HttpResponse(status))
        })
      }
      verify(errorMeter5xx, Mockito.times(2)).mark()
    }
  }

  "Upstream5xxResponse" should {
    "be counted" in new HttpErrorRateMeterTest {
      List(500, 503).foreach { status =>
        try {
          await(countErrors("servicename") {
            Future.failed(Upstream5xxResponse("foobar", status, status))
          })
        } catch {
          case ex: Throwable => ()
        }
      }
      verify(errorMeter5xx, Mockito.times(2)).mark()
    }
  }

  "HttpException" should {
    "be counted" in new HttpErrorRateMeterTest {
      List(500, 503).foreach { status =>
        try {
          await(countErrors("servicename") {
            Future.failed(new HttpException("foobar", status))
          })
        } catch {
          case ex: Throwable => ()
        }
      }
      verify(errorMeter5xx, Mockito.times(2)).mark()
    }
  }

  "Any Throwable" should {
    "also be counted" in new HttpErrorRateMeterTest {
      try {
        await(countErrors("servicename") {
          Future.failed(new RuntimeException("foobar"))
        })
      } catch {
        case ex: Throwable => ()
      }
      verify(errorMeter5xx, Mockito.times(1)).mark()
    }
  }

}

class HttpErrorRateMeterTest extends HttpErrorRateMeter with MockedKenshooRegistry {
  val errorMeter4xx = mock[Meter]
  val errorMeter5xx = mock[Meter]
  given(registry.meter("Http4xxErrorCount-servicename")).willReturn(errorMeter4xx)
  given(registry.meter("Http5xxErrorCount-servicename")).willReturn(errorMeter5xx)
}
