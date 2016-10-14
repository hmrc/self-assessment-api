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

package uk.gov.hmrc.selfassessmentapi.controllers.live.bank

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.{Interest, Bank}
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.SourceType.Banks
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.SummaryTypes.Interests
import uk.gov.hmrc.selfassessmentapi.repositories.live.BanksRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}

object BankSourceHandler extends SourceHandler(Bank, Banks.name) {
  override val repository = SourceRepositoryWrapper(BanksRepository())

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Interests => Some(SummaryHandler(SummaryRepositoryWrapper(BanksRepository().InterestRepository), Interest, Interests.name))
      case _ => throw new NotImplementedException(s"${Banks.name} ${summaryType.name} is not implemented")
    }
  }
}
