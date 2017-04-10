package uk.gov.hmrc.selfassessmentapi.models.des

import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.selfassessmentapi.models.SourceId

case class Obligations(obligations: Seq[Obligation]) {
  def selfEmploymentObligationsForId(id: SourceId): Seq[Obligation] =
    obligations.filter { obligation =>
      obligation.`type` == "ITSB" &&
      obligation.id == id
    }
}

object Obligations {
  implicit val reads: Reads[Obligations] = Json.reads[Obligations]
}

case class Obligation(id: String, `type`: String, details: Seq[ObligationDetail])

object Obligation {
  implicit val reads: Reads[Obligation] = Json.reads[Obligation]
}

case class ObligationDetail(status: String,
                            inboundCorrespondenceFromDate: String,
                            inboundCorrespondenceToDate: String,
                            inboundCorrespondenceDateReceived: Option[String],
                            inboundCorrespondenceDueDate: String,
                            periodKey: String) {
  def isFulfilled: Boolean = status == "F"
}

object ObligationDetail {
  implicit val reads: Reads[ObligationDetail] = Json.reads[ObligationDetail]
}
