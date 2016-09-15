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

package uk.gov.hmrc.selfassessmentapi.jobs

import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.Configuration
import uk.gov.hmrc.selfassessmentapi.TestApplication
import uk.gov.hmrc.selfassessmentapi.jobs.DeleteExpiredDataJob.{DeleteExpiredData, DeleteExpiredDataJobConfig}
import uk.gov.hmrc.selfassessmentapi.repositories.JobHistoryMongoRepository
import uk.gov.hmrc.selfassessmentapi.services.DeleteExpiredDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class DeleteExpiredDataJobSpec extends TestApplication {

  "DeleteExpiredDataJobConfig" should {
    val config = mock[Configuration]

    "retrieve latestModifiedDate from application config" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      when(config.getInt("timeToLiveInHours")).thenReturn(Some(12))

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      deleteConfig.latestModifiedDate shouldBe DateTime.now.minusHours(12)
    }

    "throw IllegalStateException when latestModifiedDate is not configured in application config" in {
      when(config.getInt("timeToLiveInHours")).thenReturn(None)

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      an[IllegalStateException] should be thrownBy deleteConfig.latestModifiedDate
    }

    "retrieve interval from application config" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      when(config.getMilliseconds("interval")).thenReturn(Some((1 day) toMillis))

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      deleteConfig.jobInterval shouldBe (((1 day) toMillis) milliseconds)
    }

    "throw IllegalStateException when interval is not configured in application config" in {
      when(config.getMilliseconds("interval")).thenReturn(None)

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      an[IllegalStateException] should be thrownBy deleteConfig.jobInterval
    }


    "retrieve initialDelay from application config" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      when(config.getMilliseconds("initialDelay")).thenReturn(Some(0L))

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      deleteConfig.jobInitialDelay shouldBe (0 milliseconds)
    }

    "retrieve startAt from application config" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      when(config.getMilliseconds("initialDelay")).thenReturn(None)
      when(config.getString("startAt")).thenReturn(Some("03:00"))

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      (deleteConfig.jobInitialDelay.toMillis > 0) shouldBe true
    }

    "throw IllegalStateException when both initialDelay and startAt are not configured in application config" in {
      when(config.getMilliseconds("initialDelay")).thenReturn(None)
      when(config.getString("getString")).thenReturn(None)

      val deleteConfig = new DeleteExpiredDataJobConfig(config)

      an[IllegalStateException] should be thrownBy deleteConfig.jobInterval
    }
  }

  "DeleteExpiredData" should {
    val service = mock[DeleteExpiredDataService]
    val jobRepo = mock[JobHistoryMongoRepository]
    val config = mock[DeleteExpiredDataJobConfig]
    val deleteExpiredData = new DeleteExpiredData(service, jobRepo, config)

    "delegate the call to jobRepo when isLatestJobInProgress is invoked" in {
      deleteExpiredData.isLatestJobInProgress

      verify(jobRepo).isLatestJobInProgress
    }

    "should return a success message with no of records deleted " in {
      when(service.deleteExpiredData(any[DateTime]())).thenReturn(Future.successful(100))

      val message = await(deleteExpiredData.deleteExpiredData(DateTime.now))

      message shouldBe "DeleteExpiredDataJob Completed. Total count of self assessment records deleted [100]"
    }

    "should return a message with out no of records when there are no records found" in {
      when(service.deleteExpiredData(any[DateTime]())).thenReturn(Future.successful(0))

      val message = await(deleteExpiredData.deleteExpiredData(DateTime.now))

      message shouldBe "DeleteExpiredDataJob Completed. No expired records found."
    }

    "should return an error message when any exceptions occur" in {
      when(service.deleteExpiredData(any[DateTime]())).thenReturn(Future(throw new RuntimeException("Exception occurred.")))

      val message = await(deleteExpiredData.deleteExpiredData(DateTime.now))

      message shouldBe "Exception occurred."
    }
  }

}
