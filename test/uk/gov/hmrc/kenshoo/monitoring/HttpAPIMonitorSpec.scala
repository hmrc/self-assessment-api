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

import java.util.concurrent.TimeUnit

import com.codahale.metrics.{Meter, Timer}
import org.hamcrest.Matchers._
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.Matchers
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpException}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpAPIMonitorSpec extends UnitSpec with Matchers {

  implicit val hc = HeaderCarrier()

  "monitor" should {
    "record invocation rate, average response time and error rate" in new HttpAPIMonitorTest {
      given(registry.timer("Timer-servicename")).willReturn(kenshooTimer)
      given(registry.meter("Http4xxErrorCount-servicename")).willReturn(errorMeter4xx)

      try {
        await(monitor("servicename") {
          Future({Thread.sleep(10); throw new HttpException("foobar", 400)})
        })
      } catch {
        case ex: Throwable => ()
      }
      verify(kenshooTimer).update(longThat(greaterThanOrEqualTo(10000000L)), org.mockito.Matchers.eq(TimeUnit.NANOSECONDS))
      verify(errorMeter4xx).mark()
    }
  }
}

class HttpAPIMonitorTest extends HttpAPIMonitor with MockedKenshooRegistry {
  val kenshooTimer = mock[Timer]
  val errorMeter4xx = mock[Meter]
}
