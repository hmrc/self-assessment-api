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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.JsValue
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.GiftAidPaymentsConnector
import uk.gov.hmrc.selfassessmentapi.models.giftaid.GiftAidPayments
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse

import scala.concurrent.ExecutionContext.Implicits._

object GiftAidPaymentsResource extends GiftAidPaymentsResource {
  val giftAidPaymentsConnector = GiftAidPaymentsConnector
}

trait GiftAidPaymentsResource extends BaseResource {

  val giftAidPaymentsConnector: GiftAidPaymentsConnector

  def updatePayments(nino: Nino, taxYear: TaxYear): Action[JsValue] = {
    APIAction(nino, SourceType.GiftAidPayments).async(parse.json) { implicit request =>
      validate[GiftAidPayments, EmptyResponse](request.body) { giftAidPayments =>
        giftAidPaymentsConnector.update(nino, taxYear, des.giftaid.GiftAidPayments.from(giftAidPayments))
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 204 => NoContent
          }
      }
    }
  }
}
