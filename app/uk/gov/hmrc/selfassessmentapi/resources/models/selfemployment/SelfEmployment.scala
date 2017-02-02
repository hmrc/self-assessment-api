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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, ErrorCode, SourceId}
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType._

import scala.io.Source

case class SelfEmployment(id: Option[SourceId] = None,
                          accountingPeriod: AccountingPeriod,
                          accountingType: AccountingType,
                          commencementDate: LocalDate,
                          tradingName: String,
                          businessDescription: String,
                          businessAddressLineOne: String,
                          businessAddressLineTwo: Option[String],
                          businessAddressLineThree: Option[String],
                          businessAddressLineFour: Option[String],
                          businessPostcode: String)

object SelfEmployment {

  val commencementDateValidator: Reads[LocalDate] = Reads.of[LocalDate].filter(
    ValidationError("commencement date should be today or in the past", ErrorCode.DATE_NOT_IN_THE_PAST)
  )(date => date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()))

  private lazy val sicClassifications: Seq[String] =
    Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("SICs.txt")).getLines.toIndexedSeq

  private def lengthIsBetween(minLength: Int, maxLength: Int): Reads[String] =
    Reads.of[String].filter(ValidationError(s"field length must be between $minLength and $maxLength characters", ErrorCode.INVALID_FIELD_LENGTH)
    )(name => name.length <= maxLength && name.length >= minLength)

  private val validateSIC: Reads[String] =
    Reads.of[String].filter(ValidationError(s"business description must conform to the UK SIC 2007 classifications", ErrorCode.INVALID_BUSINESS_DESCRIPTION)
    )(name => sicClassifications.contains(name))

  implicit val writes: Writes[SelfEmployment] = Json.writes[SelfEmployment]

  implicit val reads: Reads[SelfEmployment] = (
    Reads.pure(None) and
      (__ \ "accountingPeriod").read[AccountingPeriod] and
      (__ \ "accountingType").read[AccountingType] and
      (__ \ "commencementDate").read[LocalDate](commencementDateValidator) and
      (__ \ "tradingName").read[String](lengthIsBetween(1, 105)) and
      (__ \ "businessDescription").read[String](validateSIC).map(_.take(35)) and // TODO: MTDSA-760 => Should this be a code instead?
      (__ \ "businessAddressLineOne").read[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineTwo").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineThree").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineFour").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessPostcode").read[String](lengthIsBetween(1, 10))
    ) (SelfEmployment.apply _)
}
