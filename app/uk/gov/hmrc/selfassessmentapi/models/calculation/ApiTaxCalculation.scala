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
                             other: OtherDetails)

object ApiTaxCalculation {
  type DetailsA = des.DetailsA
  type DetailsB = des.DetailsB
  type DetailsC = des.DetailsC
  type DetailsD = des.DetailsD
  type DetailsE = des.DetailsE
  type DetailsF = des.DetailsF
  type DetailsG = des.DetailsG
  type DetailsH = des.DetailsH
  type DetailsI = des.DetailsI

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
      other = OtherDetails(
        incomeTaxYTD = desCalc.incomeTaxYTD,
        incomeTaxThisPeriod = desCalc.incomeTaxThisPeriod
      )
    )
  }

  implicit val writes: Writes[ApiTaxCalculation] =
    (JsPath.writeNullable[DetailsA] and JsPath.writeNullable[DetailsB] and JsPath.writeNullable[DetailsC] and JsPath.writeNullable[DetailsD] and
      JsPath.writeNullable[DetailsE] and JsPath.writeNullable[DetailsF] and JsPath.writeNullable[DetailsG] and JsPath
      .writeNullable[DetailsH] and JsPath.writeNullable[DetailsI] and JsPath.write[OtherDetails])(unlift(ApiTaxCalculation.unapply))
}
