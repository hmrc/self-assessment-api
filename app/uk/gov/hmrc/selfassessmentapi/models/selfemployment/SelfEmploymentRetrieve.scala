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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.AccountingType._
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType, SourceId, _}

case class SelfEmploymentRetrieve(id: Option[SourceId] = None,
                                  accountingPeriod: AccountingPeriod,
                                  accountingType: AccountingType,
                                  commencementDate: Option[LocalDate],
                                  cessationDate: Option[LocalDate],
                                  tradingName: String,
                                  businessDescription: Option[String],
                                  businessAddressLineOne: Option[String],
                                  businessAddressLineTwo: Option[String],
                                  businessAddressLineThree: Option[String],
                                  businessAddressLineFour: Option[String],
                                  businessPostcode: Option[String])

object SelfEmploymentRetrieve {

  def from(desSelfEmployment: des.SelfEmployment): Option[SelfEmploymentRetrieve] =
    for {
      accountingType <- AccountingType.fromDes(desSelfEmployment.cashOrAccruals)
    } yield
      SelfEmploymentRetrieve(id = desSelfEmployment.incomeSourceId,
                             accountingPeriod =
                               AccountingPeriod(start = LocalDate.parse(desSelfEmployment.accountingPeriodStartDate),
                                                end = LocalDate.parse(desSelfEmployment.accountingPeriodEndDate)),
                             accountingType = accountingType,
                             commencementDate = desSelfEmployment.tradingStartDate.map(LocalDate.parse),
                             cessationDate = None,
                             tradingName = desSelfEmployment.tradingName,
                             businessDescription = desSelfEmployment.typeOfBusiness,
                             businessAddressLineOne = desSelfEmployment.addressDetails.map(_.addressLine1),
                             businessAddressLineTwo = desSelfEmployment.addressDetails.flatMap(_.addressLine2),
                             businessAddressLineThree = desSelfEmployment.addressDetails.flatMap(_.addressLine3),
                             businessAddressLineFour = desSelfEmployment.addressDetails.flatMap(_.addressLine4),
                             businessPostcode = desSelfEmployment.addressDetails.flatMap(_.postalCode))

  implicit val writes: Writes[SelfEmploymentRetrieve] = Json.writes[SelfEmploymentRetrieve]
}
