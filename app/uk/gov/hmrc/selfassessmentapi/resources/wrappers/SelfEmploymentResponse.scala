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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.{DesTransformError, DesTransformValidator, des, SourceId}
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode, SelfEmployment}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentRetrieve

case class SelfEmploymentResponse(underlying: HttpResponse) extends Response {

  def createLocationHeader(nino: Nino): Option[String] =
    (json \ "incomeSources" \\ "incomeSourceId").map(_.asOpt[String]) match {
      case Some(id) +: _ => Some(s"/self-assessment/ni/$nino/self-employments/$id")
      case _ =>
        logger.error("The 'incomeSourceId' field was not found in the response from DES.")
        None
    }

  def selfEmployment(id: SourceId): Either[DesTransformError, SelfEmploymentRetrieve] =
    json \ "businessData" match {
      case JsUndefined() => Left(EmptyBusinessData(s"Empty business data in $json"))
      case JsDefined(_json) => validateRetrieve(id, _json)
    }

  private def validateRetrieve(id: SourceId, json: JsValue) =
    json.validate[Seq[SelfEmployment]] match {
      case JsSuccess(Nil, _) =>
        Left(EmptySelfEmployments(s"Got empty list of self employment businesses from DES for self employment id $id"))
      case JsSuccess(selfEmployments, _) =>
        for {
          desSe <- selfEmployments
                    .find(_.incomeSourceId.exists(_ == id))
                    .toRight(UnmatchedIncomeId(
                      s"Could not find Self-Employment Id $id in business details returned from DES $selfEmployments"))
                    .right
         se <- (DesTransformValidator[des.SelfEmployment, SelfEmploymentRetrieve].from(desSe).left map (ex => UnableToMapAccountingType(ex.msg))
                ).right
        } yield se.copy(id = None)
      case JsError(errors) => Left(ParseError(s"Unable to parse the response from DES as Json: $errors"))
    }

  def listSelfEmployment: Either[DesTransformError, Seq[SelfEmploymentRetrieve]] =
    json \ "businessData" match {
      case JsUndefined() => Left(EmptyBusinessData(s"Empty business data in $json"))
      case JsDefined(_json) => validateList(_json)
    }

  private def validateList(json: JsValue) =
    json.validate[Seq[SelfEmployment]] match {
      case JsSuccess(Nil, _) =>
        Left(EmptySelfEmployments(s"Got empty list of self employment businesses from DES"))
      case JsSuccess(selfEmployments, _) =>
        val result = selfEmployments.toStream.map(SelfEmploymentRetrieve.from.from)
        if (result exists (_.isLeft))
          Left(
            UnableToMapAccountingType(
              s"Could not find accounting type (cash or accruals) in DES response $selfEmployments"))
        else Right(result.map(_.right.get))
      case JsError(errors) => Left(ParseError(s"Unable to parse the response from DES as Json: $errors"))
    }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)
}

case class EmptyBusinessData(msg: String) extends DesTransformError
case class EmptySelfEmployments(msg: String) extends DesTransformError
case class ParseError(msg: String) extends DesTransformError
case class UnmatchedIncomeId(msg: String) extends DesTransformError
case class UnableToMapAccountingType(msg: String) extends DesTransformError
