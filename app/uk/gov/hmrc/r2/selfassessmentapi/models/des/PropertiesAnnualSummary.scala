/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.des

import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models

sealed trait PropertiesAnnualSummary

object PropertiesAnnualSummary {
  def from(summary: models.properties.PropertiesAnnualSummary): PropertiesAnnualSummary = summary match {
    case other: models.properties.OtherPropertiesAnnualSummary => OtherPropertiesAnnualSummary.from(other)
    case fhl: models.properties.FHLPropertiesAnnualSummary => FHLPropertiesAnnualSummary.from(fhl)
  }
}

/*
 * Non-Furnished Holiday Let [a.k.a. Other] section
 * type and value declarations, using Json reads and writes
 */
case class OtherPropertiesAnnualSummary(annualAllowances: Option[OtherPropertiesAllowances],
                                        annualAdjustments: Option[OtherPropertiesAdjustments]) extends PropertiesAnnualSummary

object OtherPropertiesAnnualSummary {
  implicit val reads: Reads[OtherPropertiesAnnualSummary] = Json.reads[OtherPropertiesAnnualSummary]
  implicit val writes: Writes[OtherPropertiesAnnualSummary] = Json.writes[OtherPropertiesAnnualSummary]
  // FROM Internal -> DES
  def from(other: models.properties.OtherPropertiesAnnualSummary): OtherPropertiesAnnualSummary = {
    val allowances = other.allowances.map { allow =>
      OtherPropertiesAllowances(
        annualInvestmentAllowance = allow.annualInvestmentAllowance,
        otherCapitalAllowance = allow.otherCapitalAllowance,
        costOfReplacingDomGoods = allow.costOfReplacingDomesticItems,
        zeroEmissionGoodsVehicleAllowance = allow.zeroEmissionsGoodsVehicleAllowance,
        businessPremisesRenovationAllowance = allow.businessPremisesRenovationAllowance,
        propertyIncomeAllowance = allow.propertyAllowance
      )
    }
    val adjustments = other.adjustments.map { adj =>
      OtherPropertiesAdjustments(
        adj.lossBroughtForward,
        adj.privateUseAdjustment,
        adj.balancingCharge,
        adj.bpraBalancingCharge,
        other.other.flatMap(_.nonResidentLandlord).getOrElse(false),
        other.other.flatMap(_.rarJointLet.map(OtherPropertiesUkOtherRentARoom(_)))
      )
    }
    OtherPropertiesAnnualSummary(allowances, adjustments)
  }
}


case class OtherPropertiesAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                     otherCapitalAllowance: Option[BigDecimal] = None,
                                     costOfReplacingDomGoods: Option[BigDecimal] = None,
                                     zeroEmissionGoodsVehicleAllowance: Option[BigDecimal] = None,
                                     businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                     propertyIncomeAllowance: Option[BigDecimal] = None
                                    )

object OtherPropertiesAllowances {
  implicit val reads: Reads[OtherPropertiesAllowances] = Json.reads[OtherPropertiesAllowances]
  implicit val writes: Writes[OtherPropertiesAllowances] = Json.writes[OtherPropertiesAllowances]
}


case class OtherPropertiesAdjustments(lossBroughtForward: Option[BigDecimal] = None,
                                      privateUseAdjustment: Option[BigDecimal] = None,
                                      balancingCharge: Option[BigDecimal] = None,
                                      businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal] = None,
                                      nonResidentLandlord: Boolean = false,
                                      ukOtherRentARoom: Option[OtherPropertiesUkOtherRentARoom] = None
                                     )

case class OtherPropertiesUkOtherRentARoom(jointlyLet: Boolean)
object OtherPropertiesUkOtherRentARoom {
  implicit val reads: Reads[OtherPropertiesUkOtherRentARoom] = Json.reads[OtherPropertiesUkOtherRentARoom]
  implicit val writes: Writes[OtherPropertiesUkOtherRentARoom] = Json.writes[OtherPropertiesUkOtherRentARoom]
}

object OtherPropertiesAdjustments {
  implicit val reads: Reads[OtherPropertiesAdjustments] = Json.reads[OtherPropertiesAdjustments]
  implicit val writes: Writes[OtherPropertiesAdjustments] = Json.writes[OtherPropertiesAdjustments]
}

/*
 * Furnished Holiday Let section
 * type and value declarations using Json reads and writes
 */

case class FHLPropertiesAnnualSummary(annualAllowances: Option[FHLPropertiesAllowances],
                                      annualAdjustments: Option[FHLPropertiesAdjustments]
                                     ) extends PropertiesAnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val reads: Reads[FHLPropertiesAnnualSummary] = Json.reads[FHLPropertiesAnnualSummary]
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]
  // FROM internal -> DES
  def from(fhl: models.properties.FHLPropertiesAnnualSummary): FHLPropertiesAnnualSummary = {
    val allowances = fhl.allowances.map { allow =>
      FHLPropertiesAllowances(
        allow.annualInvestmentAllowance,
        allow.otherCapitalAllowance,
        propertyIncomeAllowance = allow.propertyAllowance,
        businessPremisesRenovationAllowance = allow.businessPremisesRenovationAllowance
      )
    }
    val adjustments = fhl.adjustments.map { adj =>
      FHLPropertiesAdjustments(
        adj.lossBroughtForward,
        adj.privateUseAdjustment,
        adj.balancingCharge,
        adj.bpraBalancingCharge,
        adj.periodOfGraceAdjustment,
        fhl.other.flatMap(_.nonResidentLandlord).getOrElse(false),
        ukFhlRentARoom = fhl.other.flatMap(_.rarJointLet.map(FHLPropertiesUkFhlRentARoom(_)))
      )
    }
    FHLPropertiesAnnualSummary(allowances, adjustments)
  }
}

case class FHLPropertiesUkFhlRentARoom(jointlyLet: Boolean)

object FHLPropertiesUkFhlRentARoom {
  implicit val reads: Reads[FHLPropertiesUkFhlRentARoom] = Json.reads[FHLPropertiesUkFhlRentARoom]
  implicit val writes: Writes[FHLPropertiesUkFhlRentARoom] = Json.writes[FHLPropertiesUkFhlRentARoom]
}

case class FHLPropertiesAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                   otherCapitalAllowance: Option[BigDecimal] = None,
                                   businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                   propertyIncomeAllowance: Option[BigDecimal] = None)

object FHLPropertiesAllowances {
  implicit val reads: Reads[FHLPropertiesAllowances] = Json.reads[FHLPropertiesAllowances]
  implicit val writes: Writes[FHLPropertiesAllowances] = Json.writes[FHLPropertiesAllowances]
}

case class FHLPropertiesAdjustments(lossBroughtForward: Option[BigDecimal] = None,
                                    privateUseAdjustment: Option[BigDecimal] = None,
                                    balancingCharge: Option[BigDecimal] = None,
                                    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal] = None,
                                    periodOfGraceAdjustment: Option[Boolean] = None,
                                    nonResidentLandlord: Boolean = false,
                                    ukFhlRentARoom: Option[FHLPropertiesUkFhlRentARoom] = None)

object FHLPropertiesAdjustments {
  implicit val reads: Reads[FHLPropertiesAdjustments] = Json.reads[FHLPropertiesAdjustments]
  implicit val writes: Writes[FHLPropertiesAdjustments] = Json.writes[FHLPropertiesAdjustments]
}

case class FHLPropertiesOther(nonResidentLandlord: Option[Boolean] = Some(false),
                              rarJointLet: Option[Boolean] = Some(false))

object FHLPropertiesOther {
  implicit val reads: Reads[FHLPropertiesOther]   = Json.reads[FHLPropertiesOther]
  implicit val writes: Writes[FHLPropertiesOther] = Json.writes[FHLPropertiesOther]
}