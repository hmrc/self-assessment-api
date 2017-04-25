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

package uk.gov.hmrc.selfassessmentapi.models.audit

import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, TaxYear}

sealed trait AuditPayload

case class PeriodicUpdateAuditPayload(nino: Nino,
                                      sourceId: SourceId,
                                      periodId: String,
                                      transactionReference: Option[String],
                                      requestPayload: JsValue) extends AuditPayload

object PeriodicUpdateAuditPayload {
  implicit val format: Format[PeriodicUpdateAuditPayload] = Json.format[PeriodicUpdateAuditPayload]
}

case class TaxCalculationAuditPayload(nino: Nino,
                                      taxYear: Option[TaxYear],
                                      calculationId: SourceId,
                                      responsePayload: Option[JsValue]) extends AuditPayload

object TaxCalculationAuditPayload {
  implicit val format: Format[TaxCalculationAuditPayload] = Json.format[TaxCalculationAuditPayload]
}
