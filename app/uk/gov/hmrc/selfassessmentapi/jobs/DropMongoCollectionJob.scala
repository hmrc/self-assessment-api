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

import play.Logger
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryRepository, SelfAssessmentRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object DropMongoCollectionJob extends ExclusiveScheduledJob {

  override val name = "DropMongoCollectionsJob"

  override lazy val initialDelay = AppContext.dropMongoCollectionJob.getMilliseconds("initialDelay").getOrElse(throw new IllegalStateException("Config key not found: initialDelay")) millisecond

  override lazy val interval = AppContext.dropMongoCollectionJob.getMilliseconds("interval").getOrElse(throw new IllegalStateException("Config key not found: interval")) millisecond

  private val reposToBeCreated = Seq(EmploymentRepository(), SelfEmploymentRepository(), FurnishedHolidayLettingsRepository(), BanksRepository(),
    BenefitsRepository(), DividendRepository(), LiabilityRepository(), SelfAssessmentRepository(), JobHistoryRepository(), UKPropertiesRepository())

  private lazy val dropMongoCollection = new RecreateMongoDatabase(reposToBeCreated)

  override lazy val isRunning = super.isRunning.flatMap(isRunning => if (isRunning) Future(true) else dropMongoCollection.isLatestJobInProgress)

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] = {
    dropMongoCollection.dropAndRecreateMongoDatabase().map { msg =>
      Logger.info(s"Finished $name.")
      Result(msg)
    }
  }


  private class RecreateMongoDatabase(reposToBeCreated: Seq[ReactiveRepository[_, _]]) {
    private val jobRepo = JobHistoryRepository()

    def isLatestJobInProgress: Future[Boolean] = {
      jobRepo.isLatestJobInProgress
    }

    def dropAndRecreateMongoDatabase(): Future[String] = {
      Logger.info(s"Starting $name drop the mongo database and created collections.")

      jobRepo.startJob().flatMap { job =>
        val dropDB = for {
          _ <- ReactiveMongoPlugin.mongoConnector.db().drop()
        } yield true

        dropDB.flatMap { dropDB =>

          val result = Future.sequence {
            reposToBeCreated.map { repo =>
              for {
                status <- repo.ensureIndexes
              } yield status
            }
          }

          result.map { resultSeq =>
            jobRepo.completeJob(job.jobNumber, 0)
            val status = resultSeq.flatten.forall(_ == true)
            if (dropDB && status)
              s"$name Completed. Dropped the mongo database and created collections successfully."
            else
              s"$name Completed. Could not drop the mongo database and create collections."
          }.recover {
            case ex: Throwable =>
              jobRepo.abortJob(job.jobNumber)
              Logger.warn(ex.getMessage)
              ex.getMessage
          }

        }

      }

    }

  }

}
