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
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AverageResponseTimer extends KenshooMetric {
  def timer[T](serviceName: String)(function: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    val start = System.nanoTime()
    function.andThen {
      case _ =>
        val duration = Duration(System.nanoTime() - start, NANOSECONDS)
        kenshooRegistry.getTimers.getOrDefault(timerName(serviceName), kenshooRegistry.timer(timerName(serviceName))).update(duration.length, duration.unit)
        Logger.debug(s"kenshoo-event::timer::${timerName(serviceName)}::duration:{'length':${duration.length}, 'unit':${duration.unit}}")
    }
  }

  private def timerName[T](serviceName: String): String = {
    s"Timer-$serviceName"
  }
}
