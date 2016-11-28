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

import java.util
import java.util.concurrent.TimeUnit

import com.codahale.metrics.Timer
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.mockito.BDDMockito.given
import org.mockito.Matchers.longThat
import org.mockito.Mockito.verify
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AverageResponseTimerSpec extends UnitSpec {

  implicit val hc = HeaderCarrier()

  "timer" should {
    "update kenshoo timer for service if one already exists" in new AverageResponseTimerTest {
      given(registry.getTimers).willReturn(new util.TreeMap[String, Timer]() {
        {
          put("Timer-servicename", kenshooTimer)
        }
      })

      await(timer("servicename") {
        Future(Thread.sleep(1000))
      })

      verify(kenshooTimer).update(longThat(greaterThanOrEqualTo(millisToNano(1000))), org.mockito.Matchers.eq(TimeUnit.NANOSECONDS))
    }

    "update kenshoo timer for service if one doesn't exists" in new AverageResponseTimerTest {
      given(registry.timer("Timer-servicename")).willReturn(kenshooTimer)

      await(timer("servicename") {
        Future(Thread.sleep(100))
      })

      verify(kenshooTimer).update(longThat(greaterThanOrEqualTo(millisToNano(100))), org.mockito.Matchers.eq(TimeUnit.NANOSECONDS))
    }
  }

  def millisToNano(millis: Long): Long = millis * Math.pow(10, 6).asInstanceOf[Long]

}

class AverageResponseTimerTest extends AverageResponseTimer with MockedKenshooRegistry {
  val kenshooTimer: Timer = mock[Timer]
}
