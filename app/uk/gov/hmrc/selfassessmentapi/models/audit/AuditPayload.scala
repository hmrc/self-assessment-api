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

sealed trait AuditPayload {
  val auditType: String
}

case class PeriodicUpdate(nino: Nino,
                          sourceId: SourceId,
                          periodId: String,
                          transactionReference: Option[String],
                          requestPayload: JsValue) extends AuditPayload {
  override val auditType: String = "submitPeriodicUpdate"
}

object PeriodicUpdate {
  implicit val format: Format[PeriodicUpdate] = Json.format[PeriodicUpdate]
}

case class AnnualSummaryUpdate(nino: Nino,
                               sourceId: SourceId,
                               taxYear: TaxYear,
                               transactionReference: Option[String],
                               requestPayload: JsValue) extends AuditPayload {
  override val auditType: String = "submitAnnualSummary"
}

object AnnualSummaryUpdate {
  implicit val format: Format[AnnualSummaryUpdate] = Json.format[AnnualSummaryUpdate]
}

case class TaxCalculationTrigger(nino: Nino,
                                 taxYear: TaxYear,
                                 calculationId: SourceId) extends AuditPayload {
  override val auditType: String = "triggerTaxCalculation"
}

object TaxCalculationTrigger {
  implicit val format: Format[TaxCalculationTrigger] = Json.format[TaxCalculationTrigger]
}

case class TaxCalculationRequest(nino: Nino,
                                 calculationId: SourceId,
                                 responsePayload: JsValue) extends AuditPayload {
  override val auditType: String = "retrieveTaxCalculation"
}

object TaxCalculationRequest {
  implicit val format: Format[TaxCalculationRequest] = Json.format[TaxCalculationRequest]
}