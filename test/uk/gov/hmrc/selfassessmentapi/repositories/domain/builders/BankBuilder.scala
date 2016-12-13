/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Bank, BankInterestSummary}
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.InterestType._
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator

case class BankBuilder(id: BSONObjectID = BSONObjectID.generate) {
  private var bank = Bank(id, id.stringify, NinoGenerator().nextNino(), taxYear, now, now)

  private def withInterests(savings: (InterestType, BigDecimal)*) = {
    bank = bank.copy(interests = bank.interests ++ savings.map(saving => BankInterestSummary("", saving._1, saving._2)))
    this
  }

  def withTaxedInterest(savings : BigDecimal*) = {
    withInterests(savings.map((Taxed, _)):_*)
  }

  def withUntaxedInterest(savings : BigDecimal*) = {
    withInterests(savings.map((Untaxed, _)):_*)
  }

  def create() = bank
}
