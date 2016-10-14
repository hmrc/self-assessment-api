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

package uk.gov.hmrc.selfassessmentapi.repositories.live

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.{Bank, Interest}

import scala.concurrent.ExecutionContext.Implicits.global

class BanksRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val repo = new BanksMongoRepository()
  private val interestRepo = repo.InterestRepository

  private val saUtr = generateSaUtr()
  private def saving = Bank.example()

  override def beforeEach(): Unit = {
    await(repo.drop)
    await(repo.ensureIndexes)
  }

  "deleteById" should {
    "return true when a bank is deleted" in {
      val id = await(repo.create(saUtr, taxYear, saving))
      val result = await(repo.delete(saUtr, taxYear, id))

      result shouldEqual true
    }

    "return false when a bank is not deleted" in {
      val result = await(repo.delete(saUtr, taxYear, "madeUpID"))
      result shouldEqual false
    }
  }

  "delete" should {
    "delete all banks for a provided utr and tax year" in {
      for {
        n <- 1 to 10
        source = saving
        id = await(repo.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      await(repo.delete(saUtr, taxYear))

      val result = await(repo.list(saUtr, taxYear))

      result shouldBe empty
    }

    "not delete banks for different utrs and tax years" in {
      val saUtr2 = generateSaUtr()
      await(repo.create(saUtr, taxYear, saving))
      val source2 = await(repo.create(saUtr2, taxYear, saving))

      await(repo.delete(saUtr, taxYear))
      val result = await(repo.list(saUtr2, taxYear))

      result.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }

  "list" should {
    "retrieve all banks for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = saving
        id = await(repo.create(saUtr, taxYear, source))
      } yield source.copy(id = Some(id))


      val result = await(repo.list(saUtr, taxYear))
      result should contain theSameElementsAs sources
    }

    "not include banks for different utr" in {
      val source1 = await(repo.create(saUtr, taxYear, saving))
      await(repo.create(generateSaUtr(), taxYear, saving))

      val result = await(repo.list(saUtr, taxYear))
      result.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    "return false when the bank does not exist" in {
      val result = await(repo.update(saUtr, taxYear, UUID.randomUUID().toString, saving))
      result shouldEqual false
    }

    "update the last modified attribute" in {
      val source = saving
      val sourceId = await(repo.create(saUtr, taxYear, source))
      val firstResult = await(repo.findById(BSONObjectID(sourceId)))
      await(repo.update(saUtr, taxYear, sourceId, source))

      val secondResult = await(repo.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      secondResult.get.lastModifiedDateTime.isEqual(firstResult.get.lastModifiedDateTime) ||
        secondResult.get.lastModifiedDateTime.isAfter(firstResult.get.lastModifiedDateTime) shouldEqual true
    }
  }

  "create bank interest" should {
    "add an interest to an empty list when source exists and return id" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example()))
      summaryId.isDefined shouldEqual true

      val result = await(interestRepo.list(saUtr, taxYear, sourceId))
      result.isDefined shouldEqual true

      result.get.headOption shouldEqual Some(Interest.example(id = summaryId))
    }

    "add an interest to the existing list when source exists and return id" in {
        val sourceId = await(repo.create(saUtr, taxYear, saving))
        val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example()))
        val summaryId1 = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example()))

        val summaries = await(interestRepo.list(saUtr, taxYear, sourceId))

        val result = summaries.get
        result should contain theSameElementsAs Seq(Interest.example(id = summaryId), Interest.example(id = summaryId1))
    }

    "return none when source bank does not exist" in {
      val summaryId = await(interestRepo.create(saUtr, taxYear, BSONObjectID.generate.stringify, Interest.example()))
      summaryId shouldEqual None
    }
  }

  "findById bank interest" should {
    "return none if the source does not exist" in {
      await(interestRepo
          .findById(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return none if the summary does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(interestRepo.findById(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
    }

    "return the summary if found" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example())).get
      val found = await(interestRepo.findById(saUtr, taxYear, sourceId, summaryId))

      found shouldEqual Some(Interest.example(id = Some(summaryId)))
    }
  }

  "list bank interest" should {
    "return empty list when source has no summaries" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(interestRepo.list(saUtr, taxYear, sourceId)) shouldEqual Some(Seq.empty)
    }

    "return none when source does not exist" in {
      await(interestRepo.list(saUtr, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
    }
  }

  "delete bank interest" should {
    "return true when the summary has been deleted" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example())).get
      await(interestRepo.delete(saUtr, taxYear, sourceId, summaryId)) shouldEqual true
    }

    "only delete the specified summary" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example())).get
      val summaryId1 = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example()))
      await(interestRepo.delete(saUtr, taxYear, sourceId, summaryId))

      val result = await(interestRepo.list(saUtr, taxYear, sourceId)).get
      result.size shouldEqual 1
      result.head shouldEqual Interest.example(id = summaryId1)
    }

    "return false when the source bank does not exist" in {
      await(interestRepo.delete(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
    }

    "return false when the summary does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      await(interestRepo.delete(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
    }
  }


  "update bank interest" should {
    "return true when the income has been updated" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example()))
      summaryId.isDefined shouldEqual true

      await(interestRepo.update(saUtr, taxYear, sourceId, summaryId.get, Interest.example())) shouldEqual true

      val result = await(interestRepo.findById(saUtr, taxYear, sourceId, summaryId.get))
      result shouldEqual Some(Interest.example(id = Some(summaryId.get)))
    }

    "only update the specified interest summary" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))
      val summaryId1 = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example())).get
      val summaryId2 = await(interestRepo.create(saUtr, taxYear, sourceId, Interest.example())).get

      await(interestRepo.update(saUtr, taxYear, sourceId, summaryId2, Interest.example())) shouldEqual true

      val result = await(interestRepo.list(saUtr, taxYear, sourceId))
      result.isDefined shouldEqual true

      result.get should contain theSameElementsAs
        Seq(Interest.example(id = Some(summaryId1)), Interest.example(id = Some(summaryId2)))
    }

    "return false when the source bank does not exist" in {
      await(
        interestRepo.update(saUtr, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify, Interest.example())) shouldEqual false
    }

    "return false when the interest summary does not exist" in {
      val sourceId = await(repo.create(saUtr, taxYear, saving))

      await(interestRepo.update(saUtr, taxYear, sourceId, BSONObjectID.generate.stringify, Interest.example())) shouldEqual false
    }
  }
}
