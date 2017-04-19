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

package uk.gov.hmrc.selfassessmentapi.models.des


import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models

sealed trait PropertiesAnnualSummary

object PropertiesAnnualSummary {
  def from(summary: models.properties.PropertiesAnnualSummary): PropertiesAnnualSummary = summary match {
    case other: models.properties.OtherPropertiesAnnualSummary => OtherPropertiesAnnualSummary.from(other)
    case fhl: models.properties.FHLPropertiesAnnualSummary => FHLPropertiesAnnualSummary.from(fhl)
  }
}

case class OtherPropertiesAnnualSummary(otherProperties: Option[OtherPropertiesAnnualSummaryDetails]) extends PropertiesAnnualSummary

object OtherPropertiesAnnualSummary {
  implicit val reads: Reads[OtherPropertiesAnnualSummary] = Json.reads[OtherPropertiesAnnualSummary]
  implicit val writes: Writes[OtherPropertiesAnnualSummary] = Json.writes[OtherPropertiesAnnualSummary]

  def from(other: models.properties.OtherPropertiesAnnualSummary): OtherPropertiesAnnualSummary = {
    val allowances = other.allowances.map { allow =>
      OtherPropertiesAllowances(
        allow.annualInvestmentAllowance,
        allow.businessPremisesRenovationAllowance,
        allow.otherCapitalAllowance,
        allow.costOfReplacingDomesticItems,
        allow.zeroEmissionsGoodsVehicleAllowance
      )
    }
    val adjustments = other.adjustments.map { adj =>
      OtherPropertiesAdjustments(
        adj.lossBroughtForward,
        adj.privateUseAdjustment,
        adj.balancingCharge
      )
    }
    OtherPropertiesAnnualSummary(Some(OtherPropertiesAnnualSummaryDetails(allowances, adjustments)))
  }
}


case class OtherPropertiesAnnualSummaryDetails(annualAllowances: Option[OtherPropertiesAllowances],
                                               annualAdjustments: Option[OtherPropertiesAdjustments])

object OtherPropertiesAnnualSummaryDetails {
  implicit val reads: Reads[OtherPropertiesAnnualSummaryDetails] = Json.reads[OtherPropertiesAnnualSummaryDetails]
  implicit val writes: Writes[OtherPropertiesAnnualSummaryDetails] = Json.writes[OtherPropertiesAnnualSummaryDetails]
}


case class OtherPropertiesAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                     businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                     otherCapitalAllowance: Option[BigDecimal] = None,
                                     costOfReplacingDomGoods: Option[BigDecimal] = None,
                                     zeroEmissionGoodsVehicleAllowance: Option[BigDecimal] = None)

object OtherPropertiesAllowances {
  implicit val reads: Reads[OtherPropertiesAllowances] = Json.reads[OtherPropertiesAllowances]
  implicit val writes: Writes[OtherPropertiesAllowances] = Json.writes[OtherPropertiesAllowances]
}


case class OtherPropertiesAdjustments(lossBroughtForward: Option[BigDecimal] = None,
                                      privateUseAdjustment: Option[BigDecimal] = None,
                                      balancingCharge: Option[BigDecimal] = None)

object OtherPropertiesAdjustments {
  implicit val reads: Reads[OtherPropertiesAdjustments] = Json.reads[OtherPropertiesAdjustments]
  implicit val writes: Writes[OtherPropertiesAdjustments] = Json.writes[OtherPropertiesAdjustments]
}


case class FHLPropertiesAnnualSummary(furnishedHolidayLettings: Option[FHLPropertiesAnnualSummaryDetails]) extends PropertiesAnnualSummary

object FHLPropertiesAnnualSummary {
  implicit val reads: Reads[FHLPropertiesAnnualSummary] = Json.reads[FHLPropertiesAnnualSummary]
  implicit val writes: Writes[FHLPropertiesAnnualSummary] = Json.writes[FHLPropertiesAnnualSummary]

  def from(fhl: models.properties.FHLPropertiesAnnualSummary): FHLPropertiesAnnualSummary = {
    val allowances = fhl.allowances.map { allow =>
      FHLPropertiesAllowances(
        allow.annualInvestmentAllowance,
        allow.otherCapitalAllowance
      )
    }
    val adjustments = fhl.adjustments.map { adj =>
      FHLPropertiesAdjustments(
        adj.lossBroughtForward,
        adj.privateUseAdjustment,
        adj.balancingCharge
      )
    }
    FHLPropertiesAnnualSummary(Some(FHLPropertiesAnnualSummaryDetails(allowances, adjustments)))
  }
}

case class FHLPropertiesAnnualSummaryDetails(annualAllowances: Option[FHLPropertiesAllowances],
                                             annualAdjustments: Option[FHLPropertiesAdjustments])

object FHLPropertiesAnnualSummaryDetails {
  implicit val reads: Reads[FHLPropertiesAnnualSummaryDetails] = Json.reads[FHLPropertiesAnnualSummaryDetails]
  implicit val writes: Writes[FHLPropertiesAnnualSummaryDetails] = Json.writes[FHLPropertiesAnnualSummaryDetails]
}

case class FHLPropertiesAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                   otherCapitalAllowance: Option[BigDecimal] = None)

object FHLPropertiesAllowances {
  implicit val reads: Reads[FHLPropertiesAllowances] = Json.reads[FHLPropertiesAllowances]
  implicit val writes: Writes[FHLPropertiesAllowances] = Json.writes[FHLPropertiesAllowances]
}

case class FHLPropertiesAdjustments(lossBroughtForward: Option[BigDecimal] = None,
                                    privateUseAdjustment: Option[BigDecimal] = None,
                                    balancingCharge: Option[BigDecimal] = None)

object FHLPropertiesAdjustments {
  implicit val reads: Reads[FHLPropertiesAdjustments] = Json.reads[FHLPropertiesAdjustments]
  implicit val writes: Writes[FHLPropertiesAdjustments] = Json.writes[FHLPropertiesAdjustments]
}
