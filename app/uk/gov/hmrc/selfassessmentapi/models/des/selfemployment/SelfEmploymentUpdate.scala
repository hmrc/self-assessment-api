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

package uk.gov.hmrc.selfassessmentapi.models.des.selfemployment

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.selfassessmentapi.models

case class SelfEmploymentUpdate(tradingName: String, typeOfBusiness: String, addressDetails: SelfEmploymentAddress)

object SelfEmploymentUpdate {
  implicit val writes: Writes[SelfEmploymentUpdate] = Json.writes[SelfEmploymentUpdate]

  def from(apiSelfEmployment: models.selfemployment.SelfEmploymentUpdate): SelfEmploymentUpdate = {
    SelfEmploymentUpdate(
      tradingName = apiSelfEmployment.tradingName,
      typeOfBusiness = apiSelfEmployment.businessDescription,
      addressDetails = SelfEmploymentAddress(
        addressLine1 = apiSelfEmployment.businessAddressLineOne,
        addressLine2 = apiSelfEmployment.businessAddressLineTwo,
        addressLine3 = apiSelfEmployment.businessAddressLineThree,
        addressLine4 = apiSelfEmployment.businessAddressLineFour,
        postalCode = Some(apiSelfEmployment.businessPostcode)
      )
    )
  }

}
