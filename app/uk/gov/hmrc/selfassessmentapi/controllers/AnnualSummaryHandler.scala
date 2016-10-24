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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonMarshaller, SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.controllers.validate
import uk.gov.hmrc.selfassessmentapi.repositories.AnnualSummaryRepository

import scala.concurrent.ExecutionContext.Implicits.global

case class AnnualSummaryHandler[T](repository: AnnualSummaryRepository[T], jsonMarshaller: JsonMarshaller[T]) {

  implicit val reads: Reads[T] = jsonMarshaller.reads
  implicit val writes: Writes[T] = jsonMarshaller.writes

  def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, jsValue: JsValue) = {
    validate[T, Boolean](jsValue) {
      repository.createOrUpdate(saUtr, taxYear, sourceId, _)
    }
  }

  def find(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId) = {
    repository.find(saUtr, taxYear, sourceId).map(_.map(Json.toJson(_)))
  }
}
