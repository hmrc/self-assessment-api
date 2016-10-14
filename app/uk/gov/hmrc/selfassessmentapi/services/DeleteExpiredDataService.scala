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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.repositories.live._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DeleteExpiredDataService(saRepo: SelfAssessmentMongoRepository, seRepo: SelfEmploymentMongoRepository,
                               uiRepo : UnearnedIncomeMongoRepository, empRepo: EmploymentMongoRepository,
                               fhlRepo: FurnishedHolidayLettingsMongoRepository, ukPropertyRepo: UKPropertiesMongoRepository,
                               divRepo: DividendMongoRepository, bankRepo: BanksMongoRepository, jobRepo: JobHistoryMongoRepository) {

  def deleteExpiredData(lastModifiedDate: DateTime): Future[Int] = {
    Logger.info(s"Deleting records older than lastModifiedDate : $lastModifiedDate ")

    jobRepo.startJob().flatMap { job =>
      val result = for {
        oldRecords <- saRepo.findOlderThan(lastModifiedDate)
        _ <- deleteRecords(oldRecords)
        _ <- jobRepo.completeJob(job.jobNumber, oldRecords.size)
      } yield oldRecords.size

      result.recover {
        case t => Await.result(abortJob(job.jobNumber, t), Duration.Inf)
      }
    }
  }

  private def deleteRecords(records: Seq[SelfAssessment]): Future[Unit] = {
    Future.successful {
      records.foreach { record =>
        saRepo.delete(record.saUtr, record.taxYear)
        seRepo.delete(record.saUtr, record.taxYear)
        uiRepo.delete(record.saUtr, record.taxYear)
        empRepo.delete(record.saUtr, record.taxYear)
        fhlRepo.delete(record.saUtr, record.taxYear)
        divRepo.delete(record.saUtr, record.taxYear)
        bankRepo.delete(record.saUtr, record.taxYear)
        ukPropertyRepo.delete(record.saUtr, record.taxYear)
      }
    }
  }

  private def abortJob(jobNumber: Int, t: Throwable) = {
    for {
      _ <- jobRepo.abortJob(jobNumber)
    } yield throw t
  }

}

object DeleteExpiredDataService {
  def apply() = new DeleteExpiredDataService(SelfAssessmentRepository(), SelfEmploymentRepository(),
                                             UnearnedIncomeRepository(), EmploymentRepository(),
                                             FurnishedHolidayLettingsRepository(), UKPropertiesRepository(),
                                             DividendRepository(), BanksRepository(), JobHistoryRepository())
}
