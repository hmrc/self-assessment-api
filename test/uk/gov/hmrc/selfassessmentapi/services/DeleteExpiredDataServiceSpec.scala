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
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{SelfEmployment => _}
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.JobStatus._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Bank, Benefits, FurnishedHolidayLettings, JobHistory, SelfAssessment, SelfEmployment, UKProperties}
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryMongoRepository, SelfAssessmentMongoRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteExpiredDataServiceSpec extends MongoEmbeddedDatabase with MockitoSugar with ScalaFutures {

  private val saRepo = new SelfAssessmentMongoRepository
  private val seRepo = new SelfEmploymentMongoRepository
  private val uiRepo = new BenefitsMongoRepository
  private val fhlRepo = new FurnishedHolidayLettingsMongoRepository
  private val ukPropertyRepo = new UKPropertiesMongoRepository
  private val divRepo = new DividendMongoRepository
  private val bankRepo = new BanksMongoRepository
  private val jobRepo = new JobHistoryMongoRepository

  private val nino = NinoGenerator().nextNino()
  private val nino2 = NinoGenerator().nextNino()
  private val nino3 = NinoGenerator().nextNino()
  private val lastModifiedDate = DateTime.now().minusWeeks(1)

  override def beforeEach() {
    dropCollection(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, bankRepo, jobRepo)
  }

  private  def dropCollection(repos : ReactiveRepository[_, _]*) {
    repos.foreach { repo =>
      await(repo.drop)
      await(repo.ensureIndexes)
    }
  }


  implicit override val patienceConfig = PatienceConfig(timeout = Span(15, Seconds), interval = Span(300, Millis))

  /*
   * Inserts a self-assessment in to the database and verifies that it is removed correctly
   * along with any additional data that was inserted in `block`.
   */
  private def withInsertSelfAssessment(block: => Unit): Unit = {
    val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(2), DateTime.now().minusMonths(2))
    val sa2 = SelfAssessment(BSONObjectID.generate, nino2, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
    val latestSa3 = SelfAssessment(BSONObjectID.generate, nino3, taxYear, DateTime.now().minusDays(1), DateTime.now().minusDays(1))

    insertSelfAssessmentRecords(sa1, sa2, latestSa3)

    val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

    whenReady(service.deleteExpiredData(lastModifiedDate)) { _ =>
      val saRecords = saRepo.findAll()

      whenReady(saRecords) { records =>
        records.size shouldBe 1
        records.head.nino shouldBe latestSa3.nino
        records.head.taxYear shouldBe latestSa3.taxYear

        block
      }
    }
  }

  "deleteExpiredData" should {

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for benefit sources" in {
      val ui1 = Benefits.create(nino, taxYear, benefit.Benefit.example())
      val ui2 = Benefits.create(nino2, taxYear, benefit.Benefit.example())
      val latestUi = Benefits.create(nino3, taxYear, benefit.Benefit.example())

      insertUnearnedIncome(ui1, ui2, latestUi)

      withInsertSelfAssessment {
        val uiRecords = uiRepo.findAll()

        whenReady(uiRecords) { records =>
          records.size shouldBe 1
          records.head.nino shouldBe latestUi.nino
          records.head.taxYear shouldBe latestUi.taxYear
        }
      }
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for self-employments" in {
      val se1 = SelfEmployment.create(nino, taxYear, selfemployment.SelfEmployment.example())
      val se2 = SelfEmployment.create(nino2, taxYear, selfemployment.SelfEmployment.example())
      val latestSe3 = SelfEmployment.create(nino3, taxYear, selfemployment.SelfEmployment.example())

      insertSelfEmploymentRecords(se1, se2, latestSe3)

      withInsertSelfAssessment {
        val seRecords = seRepo.findAll()

        whenReady(seRecords) { records =>
          records.size shouldBe 1
          records.head.nino shouldBe latestSe3.nino
          records.head.taxYear shouldBe latestSe3.taxYear
        }
      }
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for FHLs" in {
      val fhl1 = FurnishedHolidayLettings.create(nino, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())
      val fhl2 = FurnishedHolidayLettings.create(nino2, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())
      val latestFhl3 = FurnishedHolidayLettings.create(nino3, taxYear, furnishedholidaylettings.FurnishedHolidayLetting.example())

      insertFHLRecords(fhl1, fhl2, latestFhl3)

      withInsertSelfAssessment {
        val fhlRecords = fhlRepo.findAll()

        whenReady(fhlRecords) { records =>
          records.size shouldBe 1
          records.head.nino shouldBe latestFhl3.nino
          records.head.taxYear shouldBe latestFhl3.taxYear
        }
      }
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for UK properties" in {
      val property1 = UKProperties.create(nino, taxYear, ukproperty.UKProperty.example())
      val property2 = UKProperties.create(nino2, taxYear, ukproperty.UKProperty.example())
      val latestProperty3 = UKProperties.create(nino3, taxYear, ukproperty.UKProperty.example())

      insertUKPropertyRecords(property1, property2, latestProperty3)

      withInsertSelfAssessment {
        val propertyRecords = ukPropertyRepo.findAll()

        whenReady(propertyRecords) { records =>
          records.size shouldBe 1
          records.head.nino shouldBe latestProperty3.nino
          records.head.taxYear shouldBe latestProperty3.taxYear
        }
      }
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for banks" in {
      val bank1 = Bank.create(nino, taxYear)
      val bank2 = Bank.create(nino2, taxYear)
      val latestBank3 = Bank.create(nino3, taxYear)

      insertBankRecords(bank1, bank2, latestBank3)

      withInsertSelfAssessment {
        val bankRecords = bankRepo.findAll()

        whenReady(bankRecords) { records =>
          records.size shouldBe 1
          records.head.nino shouldBe latestBank3.nino
          records.head.taxYear shouldBe latestBank3.taxYear
        }
      }
    }

    "mark job as failed if there is an exception when trying to delete records from self assessment" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val saRepo = mock[SelfAssessmentMongoRepository]
      when(saRepo.findOlderThan(any[DateTime]())).thenReturn(Future.successful(Seq(sa1)))
      when(saRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(saRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from self employment" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val seRepo = mock[SelfEmploymentMongoRepository]
      await(saRepo.insert(sa1))
      when(seRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(seRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from furnished holiday lettings" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val fhlRepo = mock[FurnishedHolidayLettingsMongoRepository]
      await(saRepo.insert(sa1))
      when(fhlRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(fhlRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from uk properties" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val ukPropertyRepo = mock[UKPropertiesMongoRepository]
      await(saRepo.insert(sa1))
      when(ukPropertyRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(ukPropertyRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from benefit source" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val uiRepo = mock[BenefitsMongoRepository]
      await(saRepo.insert(sa1))
      when(uiRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(uiRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to delete records from bank" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, nino, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val bankRepo = mock[BanksMongoRepository]
      await(saRepo.insert(sa1))
      when(bankRepo.delete(any[Nino](), any[TaxYear]())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(bankRepo).delete(any[Nino](), any[TaxYear]())
    }

    "mark job as failed if there is an exception when trying to mark job as completed" in {
      val jobRepo = mock[JobHistoryMongoRepository]
      when(jobRepo.startJob()).thenReturn(Future(JobHistory(1, InProgress)))
      when(jobRepo.completeJob(1, 0)).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, uiRepo, fhlRepo, ukPropertyRepo, divRepo, bankRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(saRepo.findAll()).size shouldBe 0
      await(seRepo.findAll()).size shouldBe 0
      verify(jobRepo).abortJob(1)
    }

  }

  private def insertUnearnedIncome(records: Benefits*) = {
    records.foreach {
      record =>
        val futureWrite = uiRepo.insert(record)
        whenReady(futureWrite)(identity)
    }
  }

  private def insertSelfAssessmentRecords(records: SelfAssessment*) = {
    records.foreach {
      record =>
        val futureWrite = saRepo.insert(record)
        whenReady(futureWrite)(identity)
    }
  }

  private def insertSelfEmploymentRecords(records: SelfEmployment*) = {
    records.foreach {
      record =>
        val futureWrite = seRepo.insert(record)
        whenReady(futureWrite)(identity)
    }
  }

  private def insertFHLRecords(records: FurnishedHolidayLettings*) = {
    records.foreach { record =>
      val futureWrite = fhlRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }

  private def insertUKPropertyRecords(records: UKProperties*) = {
    records.foreach { record =>
      val futureWrite = ukPropertyRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }

  private def insertBankRecords(records: Bank*) = {
    records.foreach { record =>
      val futureWrite = bankRepo.insert(record)
      whenReady(futureWrite)(identity)
    }
  }
}
