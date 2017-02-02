/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.{Bank, Dividends, Properties, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.JobStatus._
import uk.gov.hmrc.selfassessmentapi.repositories._
import uk.gov.hmrc.selfassessmentapi.resources.models
import uk.gov.hmrc.selfassessmentapi.resources.models.banks.BankAnnualSummary
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, AccountingType, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteExpiredDataServiceSpec extends MongoEmbeddedDatabase with MockitoSugar with ScalaFutures {

  private val seRepo = new SelfEmploymentsRepository
  private val propRepo = new PropertiesRepository
  private val divRepo = new DividendsRepository
  private val banksRepo = new BanksRepository
  private val jobRepo = new JobHistoryMongoRepository

  private val service = new DeleteExpiredDataService(seRepo, propRepo, divRepo, banksRepo, jobRepo)

  private val nino = NinoGenerator().nextNino()
  private val nino2 = NinoGenerator().nextNino()
  private val nino3 = NinoGenerator().nextNino()

  private val baseInsertionDate = DateTime.now(DateTimeZone.UTC)
  private val removalDate = baseInsertionDate.minusHours(1)

  implicit override val patienceConfig = PatienceConfig(timeout = Span(15, Seconds), interval = Span(300, Millis))

  override def beforeAll(): Unit = {
    seRepo.ensureIndexes
    propRepo.ensureIndexes
    divRepo.ensureIndexes
    banksRepo.ensureIndexes
    jobRepo.ensureIndexes
  }

  private def createSelfEmployment(lastModifiedDateTime: DateTime, nino: Nino, id: BSONObjectID = BSONObjectID.generate): SelfEmployment = {
    val accountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02"))

    SelfEmployment(id, id.stringify, nino, lastModifiedDateTime,
      accountingPeriod, AccountingType.CASH, LocalDate.now, "Acme Ltd.",
      "Boxes made of corrugated cardboard (manufacture)", "1 Acme Rd.", Some("London"), Some("Greater London"), Some("United Kingdom"),
      "A9 9AA", Map.empty, Map.empty)
  }

  private def createProperties(lastModifiedDateTime: DateTime, nino: Nino, id: BSONObjectID = BSONObjectID.generate): Properties = {
    Properties(id, nino, AccountingType.CASH, lastModifiedDateTime)
  }

  private def createDividend(lastModifiedDateTime: DateTime, nino: Nino, id: BSONObjectID = BSONObjectID.generate): Dividends = {
    Dividends(BSONObjectID.generate, nino, Map(TaxYear("2016-17") -> models.dividends.Dividends(Some(500.25))), lastModifiedDateTime)
  }

  private def createBank(lastModifiedDateTime: DateTime, nino: Nino, id: BSONObjectID = BSONObjectID.generate): Bank = {
    Bank(id, id.stringify, nino, lastModifiedDateTime, Some("my-bank"), Map(taxYear -> BankAnnualSummary(Some(122.22), Some(33.45))))
  }

  "deleteExpiredData" should {

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for self-employments" in {
      val id1 = BSONObjectID.generate
      val id2 = BSONObjectID.generate
      val id3 = BSONObjectID.generate

      val se1 = createSelfEmployment(baseInsertionDate, nino, id1)
      val se2 = createSelfEmployment(baseInsertionDate.plusDays(1), nino2, id2)
      val seToRemove = createSelfEmployment(baseInsertionDate.minusDays(1), nino3, id3)

      insertSelfEmployment(se1, se2, seToRemove)

      await(service.deleteExpiredData(removalDate)) shouldBe 1

      await(seRepo.retrieve(id1.stringify, nino)) shouldBe Some(se1)
      await(seRepo.retrieve(id2.stringify, nino2)) shouldBe Some(se2)
      await(seRepo.retrieve(id3.stringify, nino3)) shouldBe None
    }

    "mark job as failed if there is an exception when trying to delete records from self employment" in {
      val mockSeRepo = mock[SelfEmploymentsRepository]
      when(mockSeRepo.deleteAllBeforeDate(any[DateTime])).thenReturn(Future.failed(new RuntimeException("oh noes")))

      val service = new DeleteExpiredDataService(mockSeRepo, propRepo, divRepo, banksRepo, jobRepo)

      a[RuntimeException] should be thrownBy await(service.deleteExpiredData(removalDate))
      await(jobRepo.find().head).status shouldBe Failed
      verify(mockSeRepo).deleteAllBeforeDate(any[DateTime])
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for properties" in {
      val prop1 = createProperties(baseInsertionDate, nino)
      val prop2 = createProperties(baseInsertionDate.plusDays(1), nino2)
      val propToRemove = createProperties(baseInsertionDate.minusDays(1), nino3)

      insertProperties(prop1, prop2, propToRemove)

      await(service.deleteExpiredData(removalDate)) shouldBe 1

      await(propRepo.retrieve(nino)) shouldBe Some(prop1)
      await(propRepo.retrieve(nino2)) shouldBe Some(prop2)
      await(propRepo.retrieve(nino3)) shouldBe None
    }

    "mark job as failed if there is an exception when trying to delete records from properties" in {
      val mockPropRepo = mock[PropertiesRepository]
      when(mockPropRepo.deleteAllBeforeDate(any[DateTime])).thenReturn(Future.failed(new RuntimeException("oh noes")))

      val service = new DeleteExpiredDataService(seRepo, mockPropRepo, divRepo, banksRepo, jobRepo)

      a[RuntimeException] should be thrownBy await(service.deleteExpiredData(removalDate))
      await(jobRepo.find().head).status shouldBe Failed
      verify(mockPropRepo).deleteAllBeforeDate(any[DateTime])
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for dividends" in {
      val div1 = createDividend(baseInsertionDate, nino)
      val div2 = createDividend(baseInsertionDate.plusDays(1), nino2)
      val divToRemove = createDividend(baseInsertionDate.minusDays(1), nino3)

      insertDividends(div1, div2, divToRemove)

      await(service.deleteExpiredData(removalDate)) shouldBe 1

      await(divRepo.retrieve(nino)) shouldBe Some(div1)
      await(divRepo.retrieve(nino2)) shouldBe Some(div2)
      await(divRepo.retrieve(nino3)) shouldBe None
    }

    "mark job as failed if there is an exception when trying to delete records from dividends" in {
      val mockDivRepo = mock[DividendsRepository]
      when(mockDivRepo.deleteAllBeforeDate(any[DateTime])).thenReturn(Future.failed(new RuntimeException("oh noes")))

      val service = new DeleteExpiredDataService(seRepo, propRepo, mockDivRepo, banksRepo, jobRepo)

      a[RuntimeException] should be thrownBy await(service.deleteExpiredData(removalDate))
      await(jobRepo.find().head).status shouldBe Failed
      verify(mockDivRepo).deleteAllBeforeDate(any[DateTime])
    }

    "delete only the expired records (older than the lastModifiedDate) and not the latest records for banks" in {
      val id1 = BSONObjectID.generate
      val id2 = BSONObjectID.generate
      val id3 = BSONObjectID.generate

      val bank1 = createBank(baseInsertionDate, nino, id1)
      val bank2 = createBank(baseInsertionDate.plusDays(1), nino2, id2)
      val bankToRemove = createBank(baseInsertionDate.minusDays(1), nino3, id3)

      insertBanks(bank1, bank2, bankToRemove)

      await(service.deleteExpiredData(removalDate)) shouldBe 1

      await(banksRepo.retrieve(id1.stringify, nino)) shouldBe Some(bank1)
      await(banksRepo.retrieve(id2.stringify, nino2)) shouldBe Some(bank2)
      await(banksRepo.retrieve(id3.stringify, nino3)) shouldBe None
    }

    "mark job as failed if there is an exception when trying to delete records from banks" in {
      val mockBankRepo = mock[BanksRepository]
      when(mockBankRepo.deleteAllBeforeDate(any[DateTime])).thenReturn(Future.failed(new RuntimeException("oh noes")))

      val service = new DeleteExpiredDataService(seRepo, propRepo, divRepo, mockBankRepo, jobRepo)

      a[RuntimeException] should be thrownBy await(service.deleteExpiredData(removalDate))
      await(jobRepo.find().head).status shouldBe Failed
      verify(mockBankRepo).deleteAllBeforeDate(any[DateTime])
    }
  }

  private def insertSelfEmployment(records: SelfEmployment*) = {
    records.foreach { record =>
      await(seRepo.create(record))
    }
  }

  private def insertProperties(records: Properties*) = {
    records.foreach { record =>
      await(propRepo.create(record))
    }
  }

  private def insertDividends(records: Dividends*) = {
    records.foreach { record =>
      await(divRepo.create(record))
    }
  }

  private def insertBanks(records: Bank*) = {
    records.foreach { record =>
      await(banksRepo.create(record))
    }
  }
}
