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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, LiabilityId, SelfAssessment, SourceType, SourceTypes, TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.controllers.{api, LiabilityError => _, LiabilityErrors => _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Benefits, Liability, SelfEmployment, _}
import uk.gov.hmrc.selfassessmentapi.repositories.live._
import uk.gov.hmrc.selfassessmentapi.services.live.TaxYearPropertiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilityService(selfEmploymentRepo: SelfEmploymentMongoRepository,
                       benefitsRepo: BenefitsMongoRepository,
                       furnishedHolidayLettingsRepo: FurnishedHolidayLettingsMongoRepository,
                       liabilityRepo: LiabilityMongoRepository,
                       ukPropertiesRepo: UKPropertiesMongoRepository,
                       savingsRepo: BanksMongoRepository,
                       taxYearPropertiesService: TaxYearPropertiesService,
                       dividendsRepo: DividendMongoRepository,
                       featureSwitch: FeatureSwitch) {

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[Either[controllers.LiabilityErrors, api.Liability]]] = {
    liabilityRepo
      .findBy(saUtr, taxYear)
      .map(_.map {
        case calculationError: LiabilityErrors =>
          Left(
            controllers.LiabilityErrors(ErrorCode.LIABILITY_CALCULATION_ERROR,
                                         "Liability calculation error",
                                         calculationError.errors.map(error =>
                                           controllers.LiabilityError(error.code, error.message))))
        case liability: Liability => Right(liability.toLiability)
      })
  }

  def calculate(saUtr: SaUtr, taxYear: TaxYear): Future[Either[LiabilityCalculationErrorId, LiabilityId]] = {
    for {
      selfEmployments <- if (isSourceEnabled(SelfEmployments)) selfEmploymentRepo.findAll(saUtr, taxYear) else Future.successful(Seq[SelfEmployment]())
      benefits <- if (isSourceEnabled(SourceTypes.Benefits)) benefitsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Benefits]())
      ukProperties <- if (isSourceEnabled(SourceTypes.UKProperties)) ukPropertiesRepo.findAll(saUtr, taxYear) else Future.successful(Seq[UKProperties]())
      dividends <- if (isSourceEnabled(SourceTypes.Dividends)) dividendsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Dividend]())
      banks <- if (isSourceEnabled(SourceTypes.Banks)) savingsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Bank]())
      furnishedHolidayLettings <- if (isSourceEnabled(SourceTypes.FurnishedHolidayLettings)) furnishedHolidayLettingsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[FurnishedHolidayLettings]())
      taxYearProperties <- taxYearPropertiesService.findTaxYearProperties(saUtr, taxYear)
      liability = Liability.create(saUtr, taxYear, SelfAssessment(selfEmployments = selfEmployments,
        ukProperties = ukProperties, benefits = benefits, furnishedHolidayLettings = furnishedHolidayLettings,
        dividends = dividends, banks = banks, taxYearProperties = taxYearProperties))
      liability <- liabilityRepo.save(LiabilityOrError(liability))
    } yield
      liability match {
        case calculationError: LiabilityErrors => Left(calculationError.liabilityCalculationErrorId)
        case liability: Liability => Right(liability.liabilityId)
      }
  }

  private[calculation] def isSourceEnabled(sourceType: SourceType) = featureSwitch.isEnabled(sourceType)

}

object LiabilityService {

  private lazy val service = new LiabilityService(SelfEmploymentRepository(),
                                                  BenefitsRepository(),
                                                  FurnishedHolidayLettingsRepository(),
                                                  LiabilityRepository(),
                                                  UKPropertiesRepository(),
                                                  BanksRepository(),
                                                  TaxYearPropertiesService(),
                                                  DividendRepository(),
                                                  FeatureSwitch(AppContext.featureSwitch))

  def apply() = service
}
