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

import org.joda.time.DateTime
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.{PensionContribution, PensionSaving}
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYearProperties, TaxYearPropertyType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoSelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global

class SelfAssessmentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach with MockitoSugar {

  private val mockFeatureSwitch = mock[FeatureSwitch]

  private val mongoRepository = new SelfAssessmentMongoRepository(mockFeatureSwitch)

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val saUtr = generateSaUtr()

  "touch" should {
    "create self assessment record if it does not exists" in {
      await(mongoRepository.touch(saUtr, taxYear))

      val sa = await(mongoRepository.findBy(saUtr, taxYear))

      sa match {
        case Some(created) =>
          await(mongoRepository.touch(saUtr, taxYear))
          val updated = await(mongoRepository.findBy(saUtr, taxYear)).get
          updated.createdDateTime shouldEqual created.createdDateTime
        case None => fail("SA not created")
      }
    }

    "update last modified if it does exist" in {
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusWeeks(1))

      await(mongoRepository.insert(sa1))
      await(mongoRepository.touch(saUtr, taxYear))

      val sa = await(mongoRepository.findBy(saUtr, taxYear))

      sa match {
        case Some(updated) => updated.lastModifiedDateTime.isAfter(sa1.lastModifiedDateTime) shouldEqual true
        case None => fail("SA does not exist")
      }
    }
  }

  "findBy" should {
    "return records matching utr and tax year" in {
      val utr2: SaUtr = generateSaUtr()
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = MongoSelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))


      val records = await(mongoRepository.findBy(saUtr, taxYear))

      records.size shouldBe 1
      records.head.saUtr shouldEqual sa1.saUtr
    }
  }

  "findOlderThan" should {
    "return records modified older than the lastModifiedDate" in {
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val sa2 = MongoSelfAssessment(BSONObjectID.generate, generateSaUtr(), taxYear, DateTime.now().minusWeeks(2), DateTime.now().minusWeeks(2))
      val sa3 = MongoSelfAssessment(BSONObjectID.generate, generateSaUtr(), taxYear, DateTime.now().minusDays(1), DateTime.now().minusDays(1))

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))
      await(mongoRepository.insert(sa3))

      val records = await(mongoRepository.findOlderThan(DateTime.now().minusWeeks(1)))

      records.size shouldBe 2
      records.head.saUtr shouldEqual sa1.saUtr
      records.last.saUtr shouldEqual sa2.saUtr
    }
  }

  "delete" should {
    "only delete records matching utr and tax year" in {
      val utr2: SaUtr = generateSaUtr()
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = MongoSelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))

      await(mongoRepository.delete(saUtr, taxYear))

      val records = await(mongoRepository.findBy(utr2, taxYear))

      records.size shouldBe 1
      records.head.saUtr shouldEqual sa2.saUtr
    }
  }

  "findTaxYearProperties" should {
    "return tax year properties matching utr and tax year" in {
      when(mockFeatureSwitch.isEnabled(any[TaxYearPropertyType])).thenReturn(true)
      val taxYearProps = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(10000.00))))
      val sa = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps))
      await(mongoRepository.insert(sa))
      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(taxYearProps)
    }

    "ignore individual tax year properties when the individual source is disabled" in {
      when(mockFeatureSwitch.isEnabled(any[TaxYearPropertyType])).thenReturn(false)
      val taxYearProps = TaxYearProperties.example()
      val sa = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps))
      await(mongoRepository.insert(sa))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldBe Some(TaxYearProperties())
    }

    "take in to account individual tax year properties when individual properties are enabled" in {
      when(mockFeatureSwitch.isEnabled(any[TaxYearPropertyType])).thenReturn(true)
      val taxYearProps = TaxYearProperties.example()
      val sa = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps))
      await(mongoRepository.insert(sa))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldBe Some(TaxYearProperties.example())
    }
  }

  "updateTaxYearProperties" should {
    "update tax year properties matching utr and tax year" in {
      val taxYearProps1 = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(10000.00))))
      val sa = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps1))
      await(mongoRepository.insert(sa))
      val taxYearProps2 = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(50000.00),
        pensionSaving = Some(PensionSaving(Some(500.00), Some(500.00))))))

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, taxYearProps2))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(taxYearProps2)
    }
  }

}
