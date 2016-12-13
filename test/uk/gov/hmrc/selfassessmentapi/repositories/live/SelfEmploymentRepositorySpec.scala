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
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{SelfEmployment, SelfEmploymentIncomeSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepository, SummaryRepository}
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{Adjustments, Allowances}

import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfEmploymentMongoRepository
  private val selfEmploymentRepository: SourceRepository[selfemployment.SelfEmployment] = mongoRepository
  private val summariesMap: Map[JsonMarshaller[_], SummaryRepository[_]] = Map(
    Income -> mongoRepository.IncomeRepository,
    Expense -> mongoRepository.ExpenseRepository,
    BalancingCharge -> mongoRepository.BalancingChargeRepository,
    GoodsAndServicesOwnUse -> mongoRepository.GoodsAndServicesOwnUseRepository
  )

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val nino = NinoGenerator().nextNino()

  def selfEmployment(): selfemployment.SelfEmployment = selfemployment.SelfEmployment.example()

  "round trip" should {
    "create and retrieve using generated id" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(nino, taxYear, source))
      val found: selfemployment.SelfEmployment = await(selfEmploymentRepository.findById(nino, taxYear, id)).get

      found.commencementDate shouldBe source.commencementDate
    }
  }

  "delete by Id" should {
    "return true when self employment is deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(nino, taxYear, source))
      val result = await(selfEmploymentRepository.delete(nino, taxYear, id))

      result shouldBe true
    }

    "return false when self employment is not deleted" in {
      val source = selfEmployment()
      val id = await(selfEmploymentRepository.create(nino, taxYear, source))
      val result = await(selfEmploymentRepository.delete(NinoGenerator().nextNino(), taxYear, id))

      result shouldBe false
    }
  }

  "delete by utr and taxYear" should {
    "delete  all self employments for utr/tax year" in {
      for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))

      await(selfEmploymentRepository.delete(nino, taxYear))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(nino, taxYear))

      found shouldBe empty
    }

    "not delete self employments for different utr" in {
      val nino2 = NinoGenerator().nextNino()
      await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
      val source2 = await(selfEmploymentRepository.create(nino2, taxYear, selfEmployment()))

      await(selfEmploymentRepository.delete(nino, taxYear))
      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(nino2, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source2)
    }
  }

  "list" should {
    "retrieve all self employments for utr/tax year" in {
      val sources = for {
        n <- 1 to 10
        source = selfEmployment()
        id = await(selfEmploymentRepository.create(nino, taxYear, source))
      } yield source.copy(id = Some(id))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(nino, taxYear))

      found should contain theSameElementsAs sources
    }

    "not include self employments for different utr" in {
      val source1 = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
      await(selfEmploymentRepository.create(NinoGenerator().nextNino(), taxYear, selfEmployment()))

      val found: Seq[selfemployment.SelfEmployment] = await(selfEmploymentRepository.list(nino, taxYear))

      found.flatMap(_.id) should contain theSameElementsAs Seq(source1)
    }
  }

  "update" should {
    def verifyUpdate(original: selfemployment.SelfEmployment, updated: selfemployment.SelfEmployment) = {
      val sourceId = await(selfEmploymentRepository.create(nino, taxYear, original))
      await(selfEmploymentRepository.update(nino, taxYear, sourceId, updated)) shouldBe true

      val found = await(selfEmploymentRepository.findById(nino, taxYear, sourceId))
      found shouldEqual Some(updated.copy(id = Some(sourceId)))

    }

    "return true when the self employment exists and has been updated" in {
      val source = selfEmployment()

      val allowances = Allowances(annualInvestmentAllowance = Some(BigDecimal(10.00)),
                                  capitalAllowanceMainPool = Some(BigDecimal(20.00)),
                                  capitalAllowanceSpecialRatePool = Some(BigDecimal(30.00)),
                                  businessPremisesRenovationAllowance = Some(BigDecimal(50.00)),
                                  enhancedCapitalAllowance = Some(BigDecimal(60.00)),
                                  allowanceOnSales = Some(BigDecimal(70.00)))

      val adjustments = Adjustments(includedNonTaxableProfits = Some(BigDecimal(10.00)),
                                    basisAdjustment = Some(BigDecimal(20.00)),
                                    overlapReliefUsed = Some(BigDecimal(30.00)),
                                    accountingAdjustment = Some(BigDecimal(40.00)),
                                    averagingAdjustment = Some(BigDecimal(50.00)),
                                    lossBroughtForward = Some(BigDecimal(60.00)),
                                    outstandingBusinessIncome = Some(BigDecimal(70.00)))

      val updatedSource = source.copy(
        commencementDate = source.commencementDate.minusMonths(1),
        allowances = Some(allowances),
        adjustments = Some(adjustments)
      )

      verifyUpdate(source, updatedSource)
    }

    // TODO: Old code broken due to change in Allowances class.
    "set allowances to None if not provided" ignore {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = None
      )

      verifyUpdate(source, updatedSource)
    }

    // TODO: Old code broken due to change in Allowances class.
    "set each allowance to None if not provided" ignore {
      val source = selfEmployment()

      val updatedSource = source.copy(
        allowances = Some(Allowances())
      )

      verifyUpdate(source, updatedSource)
    }

    // TODO: Old code broken due to change in Adjustments class.
    "set adjustments to None if not provided" ignore {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = None
      )

      verifyUpdate(source, updatedSource)
    }

    // TODO: Old code broken due to change in Adjustments class.
    "set each adjustment to None if not provided" ignore {
      val source = selfEmployment()

      val updatedSource = source.copy(
        adjustments = Some(Adjustments())
      )

      verifyUpdate(source, updatedSource)
    }

    "return false when the self employment does not exist" in {
      val result = await(selfEmploymentRepository.update(nino, taxYear, UUID.randomUUID().toString, selfEmployment()))
      result shouldEqual false
    }

    "not remove incomes" in {
      val source = SelfEmployment
        .create(nino, taxYear, selfEmployment())
        .copy(incomes = Seq(SelfEmploymentIncomeSummary(BSONObjectID.generate.stringify, IncomeType.Turnover, 10)))
      await(mongoRepository.insert(source))
      val found = await(mongoRepository.findById(nino, taxYear, source.sourceId)).get
      await(selfEmploymentRepository.update(nino, taxYear, source.sourceId, found))

      val found1 = await(mongoRepository.findById(source.id))

      found1.get.incomes should not be empty
    }

    "update last modified" in {
      val source = selfEmployment()
      val sourceId = await(selfEmploymentRepository.create(nino, taxYear, source))
      val found = await(mongoRepository.findById(BSONObjectID(sourceId)))
      await(selfEmploymentRepository.update(nino, taxYear, sourceId, source))

      val found1 = await(mongoRepository.findById(BSONObjectID(sourceId)))

      // Added the equals clauses as it was failing locally once, can fail if the test runs faster and has the same time for create and update
      found1.get.lastModifiedDateTime.isEqual(found.get.lastModifiedDateTime) || found1.get.lastModifiedDateTime
        .isAfter(found.get.lastModifiedDateTime) shouldBe true
    }
  }

  def cast[A](a: Any): A = a.asInstanceOf[A]

  "create summary" should {
    "add a summary to an empty list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary)))

        summaryId.isDefined shouldEqual true
        val dbSummaries = await(repo.list(nino, taxYear, sourceId))

        val found = dbSummaries.get
        found.headOption shouldEqual Some(summaryItem.example(id = summaryId))
      }
    }

    "add a summary to the existing list when source exists and return id" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summary1 = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary)))
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary1)))

        val summaries = await(repo.list(nino, taxYear, sourceId))

        val found = summaries.get
        found should contain theSameElementsAs Seq(summaryItem.example(id = summaryId),
                                                   summaryItem.example(id = summaryId1))
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, BSONObjectID.generate.stringify, cast(summary)))
        summaryId shouldEqual None
      }
    }
  }

  "find summary by id" should {
    "return none if the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.findById(nino, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return none if the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        await(repo.findById(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }

    "return the summary if found" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        val found = await(repo.findById(nino, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }
  }

  "list summaries" should {
    "return empty list when source has no summaries" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        await(repo.list(nino, taxYear, sourceId)) shouldEqual Some(Seq.empty)
      }
    }

    "return none when source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.list(nino, taxYear, BSONObjectID.generate.stringify)) shouldEqual None
      }
    }
  }

  "delete summary" should {
    "return true when the summary has been deleted" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        await(repo.delete(nino, taxYear, sourceId, summaryId)) shouldEqual true
      }
    }

    "only delete the specified summary" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary)))
        await(repo.delete(nino, taxYear, sourceId, summaryId))

        val found = await(repo.list(nino, taxYear, sourceId)).get
        found.size shouldEqual 1
        found.head shouldEqual summaryItem.example(id = summaryId1)
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.delete(nino, taxYear, BSONObjectID.generate.stringify, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }

    "return false when the summary does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        await(repo.delete(nino, taxYear, sourceId, BSONObjectID.generate.stringify)) shouldEqual false
      }
    }
  }

  "update income" should {
    "return true when the income has been updated" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary = summaryItem.example()
        val summaryId = await(repo.create(nino, taxYear, sourceId, cast(summary))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(nino, taxYear, sourceId, summaryId, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.findById(nino, taxYear, sourceId, summaryId))

        found shouldEqual Some(summaryItem.example(id = Some(summaryId)))
      }
    }

    "only update the specified income" in {
      for ((summaryItem, repo) <- summariesMap) {
        val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
        val summary1 = summaryItem.example()
        val summaryId1 = await(repo.create(nino, taxYear, sourceId, cast(summary1))).get
        val summary2 = summaryItem.example()
        val summaryId2 = await(repo.create(nino, taxYear, sourceId, cast(summary2))).get

        val summaryToUpdate = summaryItem.example()
        await(repo.update(nino, taxYear, sourceId, summaryId2, cast(summaryToUpdate))) shouldEqual true

        val found = await(repo.list(nino, taxYear, sourceId)).get

        found should contain theSameElementsAs Seq(summaryItem.example(id = Some(summaryId1)),
                                                   summaryItem.example(id = Some(summaryId2)))
      }
    }

    "return false when the source does not exist" in {
      for ((summaryItem, repo) <- summariesMap) {
        await(
          repo.update(nino,
                      taxYear,
                      BSONObjectID.generate.stringify,
                      BSONObjectID.generate.stringify,
                      cast(summaryItem.example()))) shouldEqual false
      }
    }

    "return false when the income does not exist" in {
      val sourceId = await(selfEmploymentRepository.create(nino, taxYear, selfEmployment()))
      for ((summaryItem, repo) <- summariesMap) {
        await(repo.update(nino, taxYear, sourceId, BSONObjectID.generate.stringify, cast(summaryItem.example()))) shouldEqual false
      }
    }
  }

}
