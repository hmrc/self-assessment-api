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
import uk.gov.hmrc.selfassessmentapi.models.CessationReason.CessationReason
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.UnableToMapAccountingType

case class SelfEmploymentRetrieve(id: Option[SourceId] = None,
                                  accountingPeriod: AccountingPeriod,
                                  accountingType: AccountingType,
                                  commencementDate: Option[LocalDate],
                                  cessationDate: Option[LocalDate],
                                  cessationReason: Option[CessationReason],
                                  tradingName: String,
                                  description: Option[String],
                                  address: Option[Address],
                                  contactDetails: Option[ContactDetails],
                                  paperless: Option[Boolean],
                                  seasonal: Option[Boolean])


object SelfEmploymentRetrieve {

  implicit val from = new DesTransformValidator[des.selfemployment.SelfEmployment, SelfEmploymentRetrieve] {
    def from(desSelfEmployment: des.selfemployment.SelfEmployment): Either[DesTransformError, SelfEmploymentRetrieve] = {
      AccountingType.fromDes(desSelfEmployment.cashOrAccruals) match {
        case Some(accountingType) =>
          Right(SelfEmploymentRetrieve(id = desSelfEmployment.incomeSourceId,
            accountingPeriod =
              AccountingPeriod(start = LocalDate.parse(desSelfEmployment.accountingPeriodStartDate),
                end = LocalDate.parse(desSelfEmployment.accountingPeriodEndDate)),
            accountingType = accountingType,
            commencementDate = desSelfEmployment.tradingStartDate.map(LocalDate.parse),
            cessationDate = desSelfEmployment.cessationDate.map(LocalDate.parse),
            cessationReason = desSelfEmployment.cessationReason.map(CessationReason.withName),
            tradingName = desSelfEmployment.tradingName,
            description = desSelfEmployment.typeOfBusiness,
            address = desSelfEmployment.addressDetails.map(Address.from),
            contactDetails = None,
            paperless = None,
            seasonal = None))
        case None => Left(UnableToMapAccountingType(s"Could not find accounting type (cash or accruals) in DES response $desSelfEmployment"))
      }
    }
  }

  implicit val writes: Writes[SelfEmploymentRetrieve] = Json.writes[SelfEmploymentRetrieve]
}
