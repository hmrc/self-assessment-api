/*
 * Copyright 2018 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.Bank
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary

class BanksRepositorySpec extends MongoEmbeddedDatabase {
  private lazy val repo = new BanksRepository()(mongo)
  private val nino = generateNino

  private def createBank(nino: Nino, lastModifiedDateTime: DateTime, id: BSONObjectID = BSONObjectID.generate): Bank = {
    Bank(id, id.stringify, nino, lastModifiedDateTime, Some("myBank"), Map(taxYear -> BankAnnualSummary(Some(50.23), Some(12.55))))
  }

  "create" should {
    "persist a bank" in {
      val id = BSONObjectID.generate
      val bank = createBank(nino, DateTime.now(DateTimeZone.UTC), id)

      await(repo.create(bank)) shouldBe true
      await(repo.retrieve(id.stringify, nino)) shouldBe Some(bank)
    }
  }

  "update" should {
    "fail if a bank with the provided ID does not exist" in {
      val id = BSONObjectID.generate
      await(repo.update(id.stringify, nino, createBank(nino, DateTime.now(DateTimeZone.UTC), id))) shouldBe false
    }

    "overwrite existing fields in bank with new data provided by the user" in {
      val id = BSONObjectID.generate
      val bank = createBank(nino, DateTime.now(DateTimeZone.UTC), id)

      await(repo.create(bank)) shouldBe true
      await(repo.retrieve(id.stringify, nino)) shouldBe Some(bank)

      val newBank = bank.copy(accountName = Some("superBank"))
      await(repo.update(id.stringify, nino, newBank)) shouldBe true

      val result = await(repo.retrieve(id.stringify, nino)).get
      result.accountName shouldBe newBank.accountName
    }
  }

  "retrieve" should {
    "return None if a bank with the provided ID does not exist" in {
      await(repo.retrieve(BSONObjectID.generate.stringify, nino)) shouldBe None
    }
  }

  "retrieveAll" should {
    "return all bank sources associated with a nino" in {
      val id1 = BSONObjectID.generate
      val id2 = BSONObjectID.generate
      val bank1 = createBank(nino, DateTime.now(DateTimeZone.UTC), id1)
      val bank2 = createBank(nino, DateTime.now(DateTimeZone.UTC), id2)

      await(repo.create(bank1)) shouldBe true
      await(repo.create(bank2)) shouldBe true
      await(repo.retrieveAll(nino)) should contain theSameElementsAs Seq(bank1, bank2)
    }

    "return an empty sequence for a nino containing no bank sources" in {
      await(repo.retrieveAll(nino)) shouldBe Seq.empty
    }
  }

  "deleteAllBeforeDate" should {
    "delete all records older than the provided DateTime object" in {
      val bankToKeepOne = createBank(nino, DateTime.now(DateTimeZone.UTC).plusDays(1))
      val bankToKeepTwo = createBank(nino, DateTime.now(DateTimeZone.UTC))
      val bankToRemoveOne = createBank(nino, DateTime.now(DateTimeZone.UTC).minusDays(1))

      await(repo.create(bankToKeepOne))
      await(repo.create(bankToRemoveOne))
      await(repo.create(bankToKeepTwo))
      await(repo.deleteAllBeforeDate(DateTime.now(DateTimeZone.UTC).minusHours(1))) shouldBe 1
      await(repo.retrieveAll(nino)) should contain theSameElementsAs Seq(bankToKeepOne, bankToKeepTwo)
    }
  }
}
