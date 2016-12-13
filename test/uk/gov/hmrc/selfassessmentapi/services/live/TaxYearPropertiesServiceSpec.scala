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

package uk.gov.hmrc.selfassessmentapi.services.live

import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, TaxYearProperties}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.SelfAssessmentMongoRepository

import scala.concurrent.Future

class TaxYearPropertiesServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockSaRepository = mock[SelfAssessmentMongoRepository]
  val mockFeatureSwitch = mock[FeatureSwitch]
  val service = new TaxYearPropertiesService(mockSaRepository, mockFeatureSwitch)

  override def beforeEach() = {
    Mockito.reset(mockSaRepository)
    Mockito.reset(mockFeatureSwitch)
  }

  val taxYearProperties = TaxYearProperties.example()

  "TaxYearPropertiesService.findTaxYearProperties" should {
    "only include properties that have been enabled" in {

      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      when(mockSaRepository.findTaxYearProperties(any[Nino], any[TaxYear])).thenReturn(Future.successful(Some(taxYearProperties)))

      val actualProperties = await(service.findTaxYearProperties(NinoGenerator().nextNino(), taxYear))

      actualProperties.flatMap(_.studentLoan) shouldBe TaxYearProperties.example().studentLoan
      actualProperties.flatMap(_.blindPerson) shouldBe TaxYearProperties.example().blindPerson
      actualProperties.flatMap(_.childBenefit) shouldBe None
      actualProperties.flatMap(_.childBenefit) shouldBe None
    }
  }

  "TaxYearPropertiesService.updateTaxYearProperties" should {
    "update the tax year properties when the provided TaxYearProperties object contains no disabled features" in {
      val nino = NinoGenerator().nextNino()

      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      val onlySwitchedOnProperties = taxYearProperties.copy(childBenefit = None).copy(taxRefundedOrSetOff = None)

      await(service.updateTaxYearProperties(nino, taxYear, onlySwitchedOnProperties))

      verify(mockSaRepository, times(1)).updateTaxYearProperties(nino, taxYear, onlySwitchedOnProperties)
    }

    "not update the tax year properties when the provided TaxYearProperties object contains disabled features" in {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      await(service.updateTaxYearProperties(NinoGenerator().nextNino(), taxYear, taxYearProperties)) shouldBe false

      verify(mockSaRepository, times(0)).updateTaxYearProperties(any[Nino], any[TaxYear], any[TaxYearProperties])
    }
  }
}
