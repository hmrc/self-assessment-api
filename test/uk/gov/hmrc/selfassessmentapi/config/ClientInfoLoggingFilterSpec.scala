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

package uk.gov.hmrc.selfassessmentapi.config

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger => LogbackLogger}
import ch.qos.logback.core.read.ListAppender
import play.api.mvc.Action
import play.api.test.FakeRequest
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.sandbox.LiabilityController._

import scala.collection.JavaConverters._

class ClientInfoLoggingFilterSpec extends UnitSpec {

  "if the X-Client-ID header is sent in the request, it" should {
    "be logged with the uri" in {

      val filter = ClientInfoLoggingFilter
      val rh = FakeRequest().withHeaders(("X-Client-ID","499baa70-b64d-439a-a7c0-558e71d7915e"))
      val action = Action(Ok("success"))

      withCaptureOfLoggingFrom(Logger) { logEvents =>
        filter.apply(action)(rh)
        logEvents.size should be(1)
      }

    }
  }


  "if the X-Client-ID header is not sent in the request, it" should {
    "not be logged with the uri" in {

      val filter = ClientInfoLoggingFilter
      val rh = FakeRequest()
      val action = Action(Ok("success"))

      withCaptureOfLoggingFrom(Logger) { logEvents =>
        filter.apply(action)(rh)
        logEvents.size should be(0)
      }

    }
  }


  private def withCaptureOfLoggingFrom(logger: LogbackLogger)(body: (=> List[ILoggingEvent]) => Any): Any = {
    val appender = new ListAppender[ILoggingEvent]()
    appender.setContext(logger.getLoggerContext)
    appender.start()
    logger.addAppender(appender)
    logger.setLevel(Level.ALL)
    logger.setAdditive(true)
    body(appender.list.asScala.toList)
  }

  private def withCaptureOfLoggingFrom(logger: LoggerLike)(body: (=> List[ILoggingEvent]) => Any): Any = withCaptureOfLoggingFrom(logger.logger.asInstanceOf[LogbackLogger])(body)

}
