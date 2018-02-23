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

package uk.gov.hmrc.selfassessmentapi.models.des.giftaid

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, __, _}
import uk.gov.hmrc.selfassessmentapi.models.giftaid

case class GiftAidPayments(totalPayments: Option[BigDecimal] = None,
                           totalOneOffPayments: Option[BigDecimal] = None,
                           totalPaymentsBeforeTaxYearStart: Option[BigDecimal] = None,
                           totalPaymentsAfterTaxYearEnd: Option[BigDecimal] = None,
                           sharesOrSecurities: Option[BigDecimal] = None,
                           ukCharityGift: Option[GiftAidUKCharityPayments] = None,
                           nonUKCharityGift: Option[GiftAidNonUKCharityPayments] = None
                          ) {
  def hasPayments = totalPayments.isDefined && totalOneOffPayments.isDefined &&
    totalPaymentsBeforeTaxYearStart.isDefined && totalPaymentsAfterTaxYearEnd.isDefined &&
    sharesOrSecurities.isDefined && ukCharityGift.isDefined &&
    (nonUKCharityGift.isDefined && nonUKCharityGift.get.hasCharities)
}

object GiftAidPayments{

  implicit val writes: Writes[GiftAidPayments] = Json.writes[GiftAidPayments]

  implicit val reads: Reads[GiftAidPayments] = (
    (__ \ "totalPayments").readNullable[BigDecimal] and
      (__ \ "totalOneOffPayments").readNullable[BigDecimal] and
      (__ \ "totalPaymentsBeforeTaxYearStart").readNullable[BigDecimal] and
      (__ \ "totalPaymentsAfterTaxYearEnd").readNullable[BigDecimal] and
      (__ \ "sharesOrSecurities").readNullable[BigDecimal] and
      (__ \ "ukCharityGift").readNullable[GiftAidUKCharityPayments] and
      (__ \ "nonUKCharityGift").readNullable[GiftAidNonUKCharityPayments]
    )(GiftAidPayments.apply _)
    .filter(ValidationError(s"Gift payments ",
      "TO  DO -- CREATE A ERROR ENUM "))(_.hasPayments)

  def from(gap: giftaid.GiftAidPayments): GiftAidPayments = {
    GiftAidPayments(totalPayments = gap.totalPayments,
      totalOneOffPayments = gap.totalOneOffPayments,
      totalPaymentsBeforeTaxYearStart = gap.totalPaymentsBeforeTaxYearStart,
      totalPaymentsAfterTaxYearEnd = gap.totalPaymentsAfterTaxYearEnd,
      sharesOrSecurities = gap.sharesOrSecurities,
      ukCharityGift = GiftAidUKCharityPayments.from(gap.ukCharityGift),
      nonUKCharityGift = GiftAidNonUKCharityPayments.from(gap.nonUKCharityGift))
  }
}

case class GiftAidUKCharityPayments(landAndBuildings: BigDecimal)

object GiftAidUKCharityPayments {

  implicit val format: Format[GiftAidUKCharityPayments] = Json.format[GiftAidUKCharityPayments]

  def from(gap: Option[giftaid.GiftAidUKCharityPayments]): Option[GiftAidUKCharityPayments] = {
    if(gap.isDefined)
      Some(GiftAidUKCharityPayments(landAndBuildings = gap.get.landAndBuildings))
    else
      None
  }
}

case class GiftAidNonUKCharityPayments(investments: Option[BigDecimal] = None, payments: Option[BigDecimal] = None){
  def hasCharities = investments.isDefined || payments.isDefined
}

object GiftAidNonUKCharityPayments {

  implicit val format: Format[GiftAidNonUKCharityPayments] = Json.format[GiftAidNonUKCharityPayments]

  def from(gap: Option[giftaid.GiftAidNonUKCharityPayments]): Option[GiftAidNonUKCharityPayments] = {
    if(gap.exists(_.hasCharities))
      Some(GiftAidNonUKCharityPayments(investments = gap.get.investments, payments = gap.get.payments))
    else
      None
  }
}