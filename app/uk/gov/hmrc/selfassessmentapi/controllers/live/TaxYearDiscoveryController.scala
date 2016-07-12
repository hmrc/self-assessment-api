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

import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureConfig}
import uk.gov.hmrc.selfassessmentapi.controllers.{BaseController, Links}
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.SelfAssessmentRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaxYearDiscoveryController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayContext
  val repository = SelfAssessmentRepository()

  final def discoverTaxYear(utr: SaUtr, taxYear: TaxYear) = Action.async { request =>
    val halLinks = buildSourceHalLinks(utr, taxYear) + HalLink("self",
                                                               discoverTaxYearHref(utr, taxYear))
    repository
      .findTaxYearProperties(utr, taxYear)
      .map(taxYearProperties =>
            Ok(halResource(taxYearProperties match {
          case Some(t) => toJson(t)
          case None => obj()
        }, halLinks)))
  }

  private def buildSourceHalLinks(utr: SaUtr, taxYear: TaxYear) = {
    SourceTypes.types.filter { source =>
      AppContext.featureSwitch.exists { config =>
        FeatureConfig(config).isSourceEnabled(source.name)
      }
    } map { source =>
      HalLink(source.name, sourceHref(utr, taxYear, source))
    }
  }

  private def validateRequest(taxYearProperties: TaxYearProperties, taxYear: String) : Option[ValidationError] = {
    if (taxYearProperties.charitableGivings.isDefined || taxYearProperties.blindPerson.isDefined ||
        taxYearProperties.studentLoan.isDefined || taxYearProperties.taxRefundedOrSetOff.isDefined ||
        taxYearProperties.childBenefit.isDefined) {
      Some(
            ValidationError(
              ONLY_PENSION_CONTRIBUTIONS_SUPPORTED,
              s"Only update of Pension Contributions is supported",
              "/taxYearProperties"
            )
      )
    } else None
  }

  final def updateTaxYearProperties(utr: SaUtr, taxYear: TaxYear) = Action.async(parse.json) {
    implicit request =>
      withJsonBody[TaxYearProperties] { taxYearProperties =>
        validateRequest(taxYearProperties, taxYear.taxYear) match {
          case Some(error) => Future.successful(BadRequest(Json.toJson(error)))
          case None =>
            repository.updateTaxYearProperties(utr, taxYear, taxYearProperties).map {
              case true => Ok(halResource(obj(), buildSourceHalLinks(utr, taxYear)))
              case false => NotFound
            }
        }
      }
  }
}
