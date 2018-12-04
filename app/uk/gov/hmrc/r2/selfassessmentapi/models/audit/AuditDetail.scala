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

package uk.gov.hmrc.r2.selfassessmentapi.models.audit

import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceId
import uk.gov.hmrc.selfassessmentapi.models.TaxYear

sealed trait AuditDetail {
  val auditType: String
  val httpStatus: Int
  val responsePayload: Option[JsValue]
}

sealed trait ExtendedAuditDetail {
  val nino: Nino
  val taxYear: String
  val userType: String
}

case class PeriodicUpdate(override val auditType: String,
                          override val httpStatus: Int,
                          nino: Nino,
                          sourceId: SourceId,
                          periodId: String,
                          affinityGroup: String,
                          agentCode: Option[String],
                          transactionReference: Option[String],
                          requestPayload: JsValue,
                          override val responsePayload: Option[JsValue])
    extends AuditDetail

object PeriodicUpdate {
  implicit val format: Format[PeriodicUpdate] = Json.format[PeriodicUpdate]
}

case class AnnualSummaryUpdate(override val auditType: String = "submitAnnualSummary",
                               override val httpStatus: Int,
                               nino: Nino,
                               sourceId: SourceId,
                               taxYear: TaxYear,
                               affinityGroup: String,
                               agentCode: Option[String],
                               transactionReference: Option[String],
                               requestPayload: JsValue,
                               override val responsePayload: Option[JsValue])
    extends AuditDetail

object AnnualSummaryUpdate {
  implicit val format: Format[AnnualSummaryUpdate] = Json.format[AnnualSummaryUpdate]
}

case class TaxCalculationTrigger(override val auditType: String = "triggerTaxCalculation",
                                 override val httpStatus: Int,
                                 nino: Nino,
                                 taxYear: TaxYear,
                                 affinityGroup: String,
                                 agentCode: Option[String],
                                 calculationId: Option[SourceId],
                                 override val responsePayload: Option[JsValue])
    extends AuditDetail

object TaxCalculationTrigger {
  implicit val format: Format[TaxCalculationTrigger] = Json.format[TaxCalculationTrigger]
}

case class TaxCalculationRequest(override val auditType: String = "retrieveTaxCalculation",
                                 override val httpStatus: Int,
                                 nino: Nino,
                                 calculationId: SourceId,
                                 affinityGroup: String,
                                 agentCode: Option[String],
                                 override val responsePayload: Option[JsValue])
    extends AuditDetail

object TaxCalculationRequest {
  implicit val format: Format[TaxCalculationRequest] = Json.format[TaxCalculationRequest]
}

case class RetrieveObligations(override val auditType: String = "retrieveObligations",
                               override val httpStatus: Int,
                               nino: Nino,
                               sourceId: Option[SourceId],
                               affinityGroup: String,
                               agentCode: Option[String],
                               override val responsePayload: Option[JsValue])
    extends AuditDetail

object RetrieveObligations {
  implicit val format: Format[RetrieveObligations] = Json.format[RetrieveObligations]
}

case class EndOfPeriodStatementDeclaration(override val auditType: String = "declareEndOfYearStatement",
                               override val httpStatus: Int,
                               nino: Nino,
                               sourceId: SourceId,
                               accountingPeriodId: String,
                               affinityGroup: String,
                               agentCode: Option[String],
                               override val responsePayload: Option[JsValue] = None)
    extends AuditDetail

object EndOfPeriodStatementDeclaration {
  implicit val format: Format[EndOfPeriodStatementDeclaration] = Json.format[EndOfPeriodStatementDeclaration]
}

object IntentToCrystallise {
  implicit val format: Format[IntentToCrystallise] = Json.format[IntentToCrystallise]
}

case class IntentToCrystallise(nino: Nino,
                                 taxYear: String,
                                 calculationId: String,
                                 userType: String,
                                 agentReferenceNumber: Option[String],
                                 `X-CorrelationId` : Option[String])
  extends ExtendedAuditDetail

object SubmitCrystallisation {
  implicit val format: Format[SubmitCrystallisation] = Json.format[SubmitCrystallisation]
}

case class SubmitCrystallisation(nino: Nino,
                               taxYear: String,
                               request: JsValue,
                               userType: String,
                               agentReferenceNumber: Option[String],
                               `X-CorrelationId` : Option[String])
  extends ExtendedAuditDetail
