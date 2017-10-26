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

package uk.gov.hmrc.selfassessmentapi.models.calculation

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.des.TaxCalculationDetail

case class OtherDetails(incomeTaxYTD: BigDecimal,
                        incomeTaxThisPeriod: BigDecimal)

object OtherDetails {
  implicit val writes: OWrites[OtherDetails] = Json.writes[OtherDetails]
}


case class ApiTaxCalculation(a: Option[ApiTaxCalculation.DetailsA],
                             b: Option[ApiTaxCalculation.DetailsB],
                             c: Option[ApiTaxCalculation.DetailsC],
                             d: Option[ApiTaxCalculation.DetailsD],
                             e: Option[ApiTaxCalculation.DetailsE],
                             f: Option[ApiTaxCalculation.DetailsF],
                             g: Option[ApiTaxCalculation.DetailsG],
                             h: Option[ApiTaxCalculation.DetailsH],
                             i: Option[ApiTaxCalculation.DetailsI],
                             j: Option[ApiTaxCalculation.DetailsJ],
                             other: OtherDetails)

object ApiTaxCalculation {

  case class PropertyIncomeSource(
    taxableIncome: BigDecimal,
    supplied: Boolean,
    finalised: Option[Boolean]
  )

  object PropertyIncomeSource {

    implicit val reads: Reads[PropertyIncomeSource] = Json.reads[PropertyIncomeSource]
    implicit val writes: Writes[PropertyIncomeSource] = Json.writes[PropertyIncomeSource]

  }

  case class SelfEmploymentIncomeSource(
    id: Option[String],
    taxableIncome: BigDecimal,
    supplied: Boolean,
    finalised: Option[Boolean]
  )

  object SelfEmploymentIncomeSource {

    implicit val reads: Reads[SelfEmploymentIncomeSource] = Json.reads[SelfEmploymentIncomeSource]
    implicit val writes: Writes[SelfEmploymentIncomeSource] = Json.writes[SelfEmploymentIncomeSource]

  }

  case class EndOfYearEstimate(
    selfEmployment: Seq[SelfEmploymentIncomeSource],
    ukProperty: Seq[PropertyIncomeSource],
    totalTaxableIncome: Option[BigDecimal],
    incomeTaxAmount: Option[BigDecimal],
    nic2: Option[BigDecimal],
    nic4: Option[BigDecimal],
    totalNicAmount: Option[BigDecimal],
    incomeTaxNicAmount: Option[BigDecimal]
  )
  
  object EndOfYearEstimate {
  
    implicit val reads: Reads[EndOfYearEstimate] = Json.reads[EndOfYearEstimate]
    implicit val writes: Writes[EndOfYearEstimate] = Json.writes[EndOfYearEstimate]
  
  }

  type DetailsA = des.DetailsA
  type DetailsB = des.DetailsB
  type DetailsC = des.DetailsC
  type DetailsD = des.DetailsD
  type DetailsE = des.DetailsE
  type DetailsF = des.DetailsF
  type DetailsG = des.DetailsG
  type DetailsH = des.DetailsH
  type DetailsI = des.DetailsI

  case class DetailsJ(eoyEstimate: Option[EndOfYearEstimate])

  object DetailsJ {
    implicit val reads: Reads[DetailsJ] = Json.reads[DetailsJ]
    implicit val writes: OWrites[DetailsJ] = Json.writes[DetailsJ]
  }

  def from(desCalc: des.TaxCalculation): uk.gov.hmrc.selfassessmentapi.models.calculation.ApiTaxCalculation = {
    uk.gov.hmrc.selfassessmentapi.models.calculation.ApiTaxCalculation(
      a = desCalc.calcDetail.map(_.a),
      b = desCalc.calcDetail.map(_.b),
      c = desCalc.calcDetail.map(_.c),
      d = desCalc.calcDetail.map(_.d),
      e = desCalc.calcDetail.map(_.e),
      f = desCalc.calcDetail.map(_.f),
      g = desCalc.calcDetail.map(_.g),
      h = desCalc.calcDetail.map(_.h),
      i = desCalc.calcDetail.map(_.i),
      j = desCalc.calcDetail.map(convertDetailsJ(_)),
      other = OtherDetails(
        incomeTaxYTD = desCalc.incomeTaxYTD,
        incomeTaxThisPeriod = desCalc.incomeTaxThisPeriod
      )
    )
  }

  private def convertDetailsJ(calcDetail: TaxCalculationDetail) = 
    DetailsJ(calcDetail.j.eoyEstimate.map(convertEstimate(_)))

  private def convertEstimate(estimate: des.EndOfYearEstimate) =
    EndOfYearEstimate(
      selfEmployment = convertSelfEmploymentSources(estimate.incomeSource),
      ukProperty = convertPropertyIncomeSources(estimate.incomeSource),
      totalTaxableIncome = estimate.totalTaxableIncome,
      incomeTaxAmount = estimate.incomeTaxAmount,
      nic2 = estimate.nic2,
      nic4 = estimate.nic4,
      totalNicAmount = estimate.totalNicAmount,
      incomeTaxNicAmount = estimate.incomeTaxNicAmount
    )

  private def convertPropertyIncomeSources(incomes: Seq[des.IncomeSource]): Seq[PropertyIncomeSource] =
    incomes
      .filter{ _.`type` == "05" }
      .map(convertPropertyIncomeSource)

  private def convertPropertyIncomeSource(income: des.IncomeSource): PropertyIncomeSource =
    PropertyIncomeSource(
      taxableIncome = income.taxableIncome,
      supplied = income.supplied,
      finalised = income.finalised
    )

  private def convertSelfEmploymentSources(incomes: Seq[des.IncomeSource]): Seq[SelfEmploymentIncomeSource] =
    incomes
      .filter{ _.`type` == "03" }
      .map(convertSelfEmploymentSource)

  private def convertSelfEmploymentSource(income: des.IncomeSource): SelfEmploymentIncomeSource =
    SelfEmploymentIncomeSource(
      id = income.id,
      taxableIncome = income.taxableIncome,
      supplied = income.supplied,
      finalised = income.finalised
    )

  implicit val writes: Writes[ApiTaxCalculation] =
    (JsPath.writeNullable[DetailsA] and JsPath.writeNullable[DetailsB] and JsPath.writeNullable[DetailsC] and JsPath.writeNullable[DetailsD] and
      JsPath.writeNullable[DetailsE] and JsPath.writeNullable[DetailsF] and JsPath.writeNullable[DetailsG] and JsPath
      .writeNullable[DetailsH] and JsPath.writeNullable[DetailsI] and JsPath.writeNullable[DetailsJ] and JsPath.write[OtherDetails])(unlift(ApiTaxCalculation.unapply))
}
