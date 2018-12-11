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

case class OtherPropertiesAnnualSummary(annualAllowances: Option[OtherPropertiesAllowances],
                                        annualAdjustments: Option[OtherPropertiesAdjustments]) extends PropertiesAnnualSummary

object OtherPropertiesAnnualSummary {
  implicit val reads: Reads[OtherPropertiesAnnualSummary] = Json.reads[OtherPropertiesAnnualSummary]
  implicit val writes: Writes[OtherPropertiesAnnualSummary] = Json.writes[OtherPropertiesAnnualSummary]

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


case class FHLPropertiesAnnualSummary(annualAllowances: Option[FHLPropertiesAllowances],
                                      annualAdjustments: Option[FHLPropertiesAdjustments],
                                      annualOther: Option[FHLPropertiesOther]) extends PropertiesAnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val reads: Reads[FHLPropertiesAnnualSummary] = Json.reads[FHLPropertiesAnnualSummary]
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]

  def from(fhl: models.properties.FHLPropertiesAnnualSummary): FHLPropertiesAnnualSummary = {
    val allowances = fhl.allowances.map { allow =>
      FHLPropertiesAllowances(
        allow.annualInvestmentAllowance,
        allow.otherCapitalAllowance,
        allow.businessPremisesRenovationAllowance,
        allow.propertyIncomeAllowance
      )
    }
    val adjustments = fhl.adjustments.map { adj =>
      FHLPropertiesAdjustments(
        adj.lossBroughtForward,
        adj.privateUseAdjustment,
        adj.balancingCharge,
        adj.bpraBalancingCharge,
        adj.periodOfGraceAdjustment
      )
    }
    val other = fhl.other.map { other => 
      FHLPropertiesOther(
        other.nonResidentLandlord,
        other.rarJointLet
      )
    }
    FHLPropertiesAnnualSummary(allowances, adjustments, other)
  }
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
                                    bpraBalancingCharge: Option[BigDecimal] = None,
                                    periodOfGraceAdjustment: Option[Boolean] = None)

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