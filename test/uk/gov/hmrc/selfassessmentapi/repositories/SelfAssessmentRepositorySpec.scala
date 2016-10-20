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

import org.joda.time.{DateTime, LocalDate}
import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYearProperties, UkCountryCodes}
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionSaving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoanPlanType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.TaxYearPropertiesBuilder

class SelfAssessmentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfAssessmentMongoRepository

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

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
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusWeeks(1))

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
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = SelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))


      val records = await(mongoRepository.findBy(saUtr, taxYear))

      records.size shouldBe 1
      records.head.saUtr shouldEqual sa1.saUtr
    }
  }

  "findOlderThan" should {
    "return records modified older than the lastModifiedDate" in {
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val sa2 = SelfAssessment(BSONObjectID.generate, generateSaUtr(), taxYear, DateTime.now().minusWeeks(2), DateTime.now().minusWeeks(2))
      val sa3 = SelfAssessment(BSONObjectID.generate, generateSaUtr(), taxYear, DateTime.now().minusDays(1), DateTime.now().minusDays(1))

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
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = SelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

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
      val taxYearProps = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(10000.00))))
      val sa = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps))
      await(mongoRepository.insert(sa))
      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(taxYearProps)
    }
  }

  "updateTaxYearProperties" should {
    "update the pension contribution for tax year properties matching utr and tax year" in {
      val taxYearProps1 = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(10000.00))))
      val sa = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now(), Some(taxYearProps1))
      await(mongoRepository.insert(sa))
      val taxYearProps2 = TaxYearProperties(pensionContributions = Some(PensionContribution(ukRegisteredPension = Some(50000.00),
        pensionSavings = Some(PensionSaving(Some(500.00), Some(500.00))))))

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, taxYearProps2))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(taxYearProps2)
    }

    "update the charitable givings for tax year properties matching utr and tax year" in {
      val charGivingsBefore = TaxYearPropertiesBuilder()
        .withCharitableGivings()
        .giftAidPayments(
          totalInTaxYear = 100,
          oneOff = 50,
          toNonUkCharities = 20,
          carriedBackToPreviousTaxYear = 20,
          carriedFromNextTaxYear = 20)
        .sharesSecurities(
          totalInTaxYear = 500,
          toNonUkCharities = 50)
        .landAndProperties(
          totalInTaxYear = 10000,
          toNonUkCharities = 5000)
        .create()

      val selfAssessment =
        SelfAssessment(BSONObjectID.generate, saUtr, taxYear, now, now, Some(charGivingsBefore))

      await(mongoRepository.insert(selfAssessment))

      val charGivingsAfter = TaxYearPropertiesBuilder()
        .withCharitableGivings()
        .giftAidPayments(
          totalInTaxYear = 120,
          oneOff = 55,
          toNonUkCharities = 26,
          carriedBackToPreviousTaxYear = 10,
          carriedFromNextTaxYear = 22)
        .sharesSecurities(
          totalInTaxYear = 500.50,
          toNonUkCharities = 55)
        .landAndProperties(
          totalInTaxYear = 10000.22,
          toNonUkCharities = 500)
        .create()


      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, charGivingsAfter))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(charGivingsAfter)
    }

    "update the blind person for tax year properties matching utr and tax year" in {
      val blindPersonBefore = TaxYearPropertiesBuilder()
        .withBlindPerson(
          country = UkCountryCodes.England,
          registrationAuthority = "cake",
          spouseSurplusAllowance = 1500.25,
          wantsSpouseToUseSurplusAllowance = true)
        .create()

      val selfAssessment = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, now, now, Some(blindPersonBefore))

      await(mongoRepository.insert(selfAssessment))

      val blindPersonAfter = TaxYearPropertiesBuilder()
        .withBlindPerson(
          country = UkCountryCodes.Wales,
          registrationAuthority = "lie",
          spouseSurplusAllowance = 2100.05,
          wantsSpouseToUseSurplusAllowance = false)
        .create()

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, blindPersonAfter))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(blindPersonAfter)
    }

    "update the student loan for tax year properties matching utr and tax year" in {
      val studentLoanBefore = TaxYearPropertiesBuilder()
        .withStudentLoan(
          planType = StudentLoanPlanType.Plan1,
          deductedByEmployers = 20.20)
        .create()

      val selfAssessment = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, now, now, Some(studentLoanBefore))

      await(mongoRepository.insert(selfAssessment))

      val studentLoanAfter = TaxYearPropertiesBuilder()
        .withStudentLoan(
          planType = StudentLoanPlanType.Plan2,
          deductedByEmployers = 100.50)
        .create()

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, studentLoanAfter))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(studentLoanAfter)
    }

    "update the tax refunded or set off for tax year properties matching utr and tax year" in {
      val taxRefundedBefore = TaxYearPropertiesBuilder()
        .withTaxRefundedOrSetOff(500)
        .create()

      val selfAssessment = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, now, now, Some(taxRefundedBefore))

      await(mongoRepository.insert(selfAssessment))

      val taxRefundedAfter = TaxYearPropertiesBuilder()
        .withTaxRefundedOrSetOff(20.20)
        .create()

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, taxRefundedAfter))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(taxRefundedAfter)
    }

    "update the child benefit for tax year properties matching utr and tax year" in {
      val childBenefitBefore = TaxYearPropertiesBuilder()
        .withChildBenefit(
          amount = 2500,
          numberOfChildren = 2,
          dateBenefitStopped = now.toLocalDate)
        .create()

      val selfAssessment = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, now, now, Some(childBenefitBefore))

      await(mongoRepository.insert(selfAssessment))

      val childBenefitAfter = TaxYearPropertiesBuilder()
        .withChildBenefit(
          amount = 1000,
          numberOfChildren = 1,
          dateBenefitStopped = new LocalDate(2012, 12, 12))
        .create()

      await(mongoRepository.updateTaxYearProperties(saUtr, taxYear, childBenefitAfter))

      val records = await(mongoRepository.findTaxYearProperties(saUtr, taxYear))
      records shouldEqual Some(childBenefitAfter)
    }
  }
}
