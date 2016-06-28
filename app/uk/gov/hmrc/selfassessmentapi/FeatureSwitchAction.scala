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

package uk.gov.hmrc.selfassessmentapi

import play.api.mvc.{ActionBuilder, Request, Result}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.domain.SourceType
import play.api.mvc.Results._
import scala.concurrent.Future

class FeatureSwitchAction(source: SourceType, summary: String) extends ActionBuilder[Request] {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    if (FeatureSwitch(AppContext.featureSwitch).isEnabled(source, summary)) block(request)
    else Future.successful(NotFound)
  }
}

object FeatureSwitchAction {
  def apply(source: SourceType, summary: String = "") = new FeatureSwitchAction(source, summary)
}
