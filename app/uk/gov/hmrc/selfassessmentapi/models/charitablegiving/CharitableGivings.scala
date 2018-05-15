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

package uk.gov.hmrc.selfassessmentapi.models.charitablegiving

import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, __, _}
import uk.gov.hmrc.selfassessmentapi.models.{des, _}

case class CharitableGivings(giftAidPayments: Option[GiftAidPayments],
                             gifts: Option[Gifts])

object CharitableGivings{
  implicit val format: Format[CharitableGivings] = Json.format[CharitableGivings]

  def from(desCharitableGivings: des.charitablegiving.CharitableGivings): CharitableGivings = {
    CharitableGivings(giftAidPayments = GiftAidPayments.from(desCharitableGivings.giftAidPayments),
      gifts = Gifts.from(desCharitableGivings.gifts))
  }
}

case class GiftAidPayments(currentYear: Option[BigDecimal] = None,
                           oneOffCurrentYear: Option[BigDecimal] = None,
                           currentYearTreatedAsPreviousYear: Option[BigDecimal] = None,
                           nextYearTreatedAsCurrentYear: Option[BigDecimal] = None,
                           nonUKCharities: Option[BigDecimal] = None)

object GiftAidPayments{

  implicit val writes: Writes[GiftAidPayments] = Json.writes[GiftAidPayments]

  implicit val reads: Reads[GiftAidPayments] = (
    (__ \ "currentYear").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "oneOffCurrentYear").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "currentYearTreatedAsPreviousYear").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "nextYearTreatedAsCurrentYear").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "nonUKCharities").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings)
    )(GiftAidPayments.apply _)

  def from(desGAP: Option[des.charitablegiving.GiftAidPayments]): Option[GiftAidPayments] = {
    desGAP match {
      case Some(payments) =>
        Some(GiftAidPayments(currentYear = payments.currentYear,
        oneOffCurrentYear = payments.oneOffCurrentYear,
        currentYearTreatedAsPreviousYear = payments.currentYearTreatedAsPreviousYear,
        nextYearTreatedAsCurrentYear = payments.nextYearTreatedAsCurrentYear,
        nonUKCharities = payments.nonUKCharities))
      case None => None
    }
  }
}

case class Gifts(landAndBuildings: Option[BigDecimal] = None,
                 sharesOrSecurities: Option[BigDecimal] = None,
                 investmentsNonUKCharities: Option[BigDecimal] = None)

object Gifts {

  implicit val writes: Writes[Gifts] = Json.writes[Gifts]

  implicit val reads: Reads[Gifts] = (
    (__ \ "landAndBuildings").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "sharesOrSecurities").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings) and
      (__ \ "investmentsNonUKCharities").readNullable[BigDecimal](nonNegativeAmountValidatorForCharitableGivings)
    )(Gifts.apply _)

  def from(desGifts: Option[des.charitablegiving.Gifts]): Option[Gifts] = {
    desGifts match {
      case Some(gifts) =>
        Some(Gifts(landAndBuildings = gifts.landAndBuildings,
        sharesOrSecurities = gifts.sharesOrSecurities,
        investmentsNonUKCharities = gifts.investmentsNonUKCharities))
      case None => None
    }
  }
}
