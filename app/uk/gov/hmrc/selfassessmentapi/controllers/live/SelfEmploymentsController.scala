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

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmploymentId
import uk.gov.hmrc.selfassessmentapi.services.live.SelfEmploymentService

object SelfEmploymentsController extends uk.gov.hmrc.selfassessmentapi.controllers.SelfEmploymentsController {
  override val selfEmploymentService = SelfEmploymentService

  override def create(saUtr: SaUtr) = Action(parse.json) { request =>
    NotImplemented(Json.toJson(ErrorNotImplemented))
  }

  override def findById(utr: SaUtr, seId: SelfEmploymentId) = validateAccept(acceptHeaderValidationRules) {
    NotImplemented(Json.toJson(ErrorNotImplemented))
  }

  override def update(saUtr: SaUtr, seId: SelfEmploymentId) = Action(parse.json) { request =>
    NotImplemented(Json.toJson(ErrorNotImplemented))
  }

  override def delete(utr: SaUtr, seId: SelfEmploymentId) = validateAccept(acceptHeaderValidationRules) {
    NotImplemented(Json.toJson(ErrorNotImplemented))
  }
}
