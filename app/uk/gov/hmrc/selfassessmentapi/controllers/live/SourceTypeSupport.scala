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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.SourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceType
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.live.bank.BankSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.benefit.BenefitSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.dividend.DividendSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.furnishedholidaylettings.FurnishedHolidayLettingsSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.selfemployment.SelfEmploymentSourceHandler
import uk.gov.hmrc.selfassessmentapi.controllers.live.ukproperty.UKPropertySourceHandler

trait SourceTypeSupport extends uk.gov.hmrc.selfassessmentapi.controllers.SourceTypeSupport {
  def sourceHandler(sourceType: SourceType): SourceHandler[_] = sourceType match {
    case SelfEmployments => SelfEmploymentSourceHandler
    case Benefits => BenefitSourceHandler
    case FurnishedHolidayLettings => FurnishedHolidayLettingsSourceHandler
    case UKProperties => UKPropertySourceHandler
    case Dividends => DividendSourceHandler
    case Banks => BankSourceHandler
    case _ => throw new NotImplementedException(s"${sourceType.name} is not implemented")
  }
}
