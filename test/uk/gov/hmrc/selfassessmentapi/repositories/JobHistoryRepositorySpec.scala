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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeUtils}
import org.scalatest.BeforeAndAfterEach
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.repositories.domain.JobHistory
import uk.gov.hmrc.selfassessmentapi.repositories.domain.JobStatus._

import scala.concurrent.ExecutionContext.Implicits.global

class JobHistoryRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new JobHistoryMongoRepository

  override def beforeEach() = {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  "insert" should {

    "throw exception when trying to insert a job with the same number" in {
      await(mongoRepository.insert(JobHistory(1, InProgress)))

      an[DatabaseException] should be thrownBy await(mongoRepository.insert(JobHistory(1, InProgress)))
    }
  }

  "startJob" should {

    "start a new job if there are no jobs" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      val jobHistory = await(mongoRepository.startJob())

      jobHistory shouldBe JobHistory(jobNumber = 1, status = InProgress, startedAt = DateTime.now,  finishedAt = None, recordsDeleted = 0)
    }

    "start a new job if last job has completed" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      await(mongoRepository.insert(JobHistory(jobNumber = 1, status = Success)))

      val jobHistory = await(mongoRepository.startJob())

      jobHistory shouldBe JobHistory(jobNumber = 2, status = InProgress, startedAt = DateTime.now,  finishedAt = None, recordsDeleted = 0)
    }

    "start a new job if last job has failed" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      await(mongoRepository.insert(JobHistory(jobNumber = 1, status = Failed)))

      val jobHistory = await(mongoRepository.startJob())

      jobHistory shouldBe JobHistory(jobNumber = 2, status = InProgress, startedAt = DateTime.now,  finishedAt = None, recordsDeleted = 0)
    }

    "throw exception if last job is still in progress" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      await(mongoRepository.startJob())

      an[JobAlreadyInProgressException] should be thrownBy await(mongoRepository.startJob())
    }
  }

  "completeJob" should {

    "mark job as completed and update the records deleted" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      await(mongoRepository.insert(JobHistory(1, InProgress)))

      await(mongoRepository.completeJob(1, 100))

      val actualJob = await(mongoRepository.find("jobNumber" -> 1)).head

      actualJob shouldBe JobHistory(jobNumber = 1, status = Success, startedAt = DateTime.now,
        finishedAt = Some(DateTime.now), recordsDeleted = 100)
    }

    "throw exception if job with given number does not exist" in {
      an[JobNotFoundException] should be thrownBy await(mongoRepository.completeJob(101, 0))
    }
  }

  "abortJob" should {

    "mark job as failed" in {
      DateTimeUtils.setCurrentMillisFixed(DateTime.now.getMillis)

      await(mongoRepository.insert(JobHistory(1, InProgress)))

      await(mongoRepository.abortJob(1))

      val actualJob = await(mongoRepository.find("jobNumber" -> 1)).head

      actualJob shouldBe JobHistory(jobNumber = 1, status = Failed, startedAt = DateTime.now,
        finishedAt = Some(DateTime.now), recordsDeleted = 0)
    }

    "throw exception if job with given number does not exist" in {
      an[JobNotFoundException] should be thrownBy await(mongoRepository.abortJob(101))
    }
  }

  "isLatestJobInProgress" should {

    "return false if there are no jobs" in {
      await(mongoRepository.isLatestJobInProgress) shouldBe false
    }

    "return true if last job is still in progress" in {
      await(mongoRepository.insert(JobHistory(1, InProgress)))

      await(mongoRepository.isLatestJobInProgress) shouldBe true
    }


    "return false if last job has completed successfully" in {
      await(mongoRepository.insert(JobHistory(1, Success)))

      await(mongoRepository.isLatestJobInProgress) shouldBe false
    }

    "return false if last job has completed with errors" in {
      await(mongoRepository.insert(JobHistory(1, Failed)))

      await(mongoRepository.isLatestJobInProgress) shouldBe false
    }
  }


}
