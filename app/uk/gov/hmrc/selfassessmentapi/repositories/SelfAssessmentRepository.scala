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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeZone}
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDouble, BSONElement, BSONInteger, BSONNull, BSONObjectID, BSONString, BSONValue, Producer}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, TaxYearProperties}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfAssessmentRepository extends MongoDbConnection {
  private lazy val repository = new SelfAssessmentMongoRepository

  def apply() = repository
}

class SelfAssessmentMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SelfAssessment, BSONObjectID](
    "selfAssessments",
    mongo,
    domainFormat = SelfAssessment.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with AtomicUpdate[SelfAssessment] {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("sa_nino_taxyear"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("sa_last_modified"), unique = false))


  def touch(nino: Nino, taxYear: TaxYear) = {

    for {
      result <- atomicUpsert(
        BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString)),
        touchModifier()
      )
    } yield ()
  }

  private def touchModifier(): BSONDocument = {
    val now = DateTime.now(DateTimeZone.UTC)
    BSONDocument(
      setOnInsert(now),
      "$set" -> BSONDocument(lastModifiedDateTimeModfier(now))
    )
  }

  private def setOnInsert(dateTime: DateTime): Producer[BSONElement] =
    "$setOnInsert" -> BSONDocument("createdDateTime" -> BSONDateTime(dateTime.getMillis))

  private def lastModifiedDateTimeModfier(dateTime: DateTime): Producer[BSONElement] =
    "lastModifiedDateTime" -> BSONDateTime(dateTime.getMillis)


  def findBy(nino: Nino, taxYear: TaxYear): Future[Option[SelfAssessment]] = {
    find(
      "nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString)
    ).map(_.headOption)
  }

  def findOlderThan(lastModified: DateTime): Future[Seq[SelfAssessment]] = {
    find(
      "lastModifiedDateTime" -> BSONDocument("$lt" -> BSONDateTime(lastModified.getMillis))
    )
  }

  def delete(nino: Nino, taxYear: TaxYear): Future[Boolean] = {
    for (option <- remove("nino" -> nino.nino, "taxYear" -> taxYear.taxYear)) yield option.n > 0
  }

  def isInsertion(suppliedId: BSONObjectID, returned: SelfAssessment): Boolean = suppliedId.equals(returned.id)

  def updateTaxYearProperties(nino: Nino, taxYear: TaxYear, taxYearProperties: TaxYearProperties): Future[Unit] = {
    val now = DateTime.now(DateTimeZone.UTC)

    for {
      result <- atomicUpsert(
        BSONDocument("nino" -> nino.nino, "taxYear" -> taxYear.taxYear),
        BSONDocument(
          setOnInsert(now),
          "$set" -> constructTaxYearPropertiesBson(taxYearProperties, now)
        ))
    } yield ()
  }

  private def constructTaxYearPropertiesBson(taxYearProperties: TaxYearProperties, now: DateTime): BSONDocument = {
    BSONDocument(
      lastModifiedDateTimeModfier(now),
      "taxYearProperties" -> BSONDocument(
        taxYearPensionContributionsBson(taxYearProperties),
        taxYearCharitableGivingsBson(taxYearProperties),
        taxYearBlindPersonBson(taxYearProperties),
        taxYearStudentLoanBson(taxYearProperties),
        taxYearTaxRefundedOrSetOffBson(taxYearProperties),
        taxYearChildBenefitBson(taxYearProperties)
      )
    )
  }

  private def taxYearPensionContributionsBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.pensionContributions
      .map(pensionContributions =>
            "pensionContributions" -> BSONDocument(
              Seq(
                "ukRegisteredPension" -> pensionContributions.ukRegisteredPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "retirementAnnuity" -> pensionContributions.retirementAnnuity.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "employerScheme" -> pensionContributions.employerScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "overseasPension" -> pensionContributions.overseasPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "pensionSavings" -> BSONDocument(Seq(
                  "excessOfAnnualAllowance" -> pensionContributions.pensionSavings.map(_.excessOfAnnualAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull),
                  "taxPaidByPensionScheme" -> pensionContributions.pensionSavings.map(_.taxPaidByPensionScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull)
                )))))
      .getOrElse("pensionContributions" -> BSONNull)
  }

  private def taxYearCharitableGivingsBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.charitableGivings
      .map(charitableGivings =>
        "charitableGivings" -> BSONDocument(Seq(
            "giftAidPayments" -> charitableGivings.giftAidPayments.map(giftAid =>
              BSONDocument(Seq(
                "totalInTaxYear" -> giftAid.totalInTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "oneOff" -> giftAid.oneOff.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "toNonUkCharities" -> giftAid.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "carriedBackToPreviousTaxYear" -> giftAid.carriedBackToPreviousTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                "carriedFromNextTaxYear" -> giftAid.carriedFromNextTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
              ))).getOrElse(BSONNull),
            "sharesSecurities" -> charitableGivings.sharesSecurities.map(sharesAndSecs =>
              BSONDocument(Seq(
                "totalInTaxYear" -> BSONDouble(sharesAndSecs.totalInTaxYear.doubleValue()),
                "toNonUkCharities" -> sharesAndSecs.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
              ))).getOrElse(BSONNull),
            "landProperties" -> charitableGivings.landProperties.map(landProperties =>
            BSONDocument(Seq(
              "totalInTaxYear" -> BSONDouble(landProperties.totalInTaxYear.doubleValue()),
              "toNonUkCharities" -> landProperties.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
            ))).getOrElse(BSONNull)
          ))).getOrElse("charitableGivings" -> BSONNull)
  }

  private def taxYearBlindPersonBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.blindPerson
      .map(blindPerson =>
      "blindPerson" -> BSONDocument(Seq(
        "country" -> blindPerson.country.map(x => BSONString(x.toString)).getOrElse(BSONNull),
        "registrationAuthority" -> blindPerson.registrationAuthority.map(BSONString).getOrElse(BSONNull),
        "spouseSurplusAllowance" -> blindPerson.spouseSurplusAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
        "wantSpouseToUseSurplusAllowance" -> blindPerson.wantSpouseToUseSurplusAllowance.map(BSONBoolean).getOrElse(BSONNull)
      ))).getOrElse("blindPerson" -> BSONNull)
  }

  private def taxYearStudentLoanBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.studentLoan
      .map(loan =>
      "studentLoan" -> BSONDocument(Seq(
        "planType" -> BSONString(loan.planType.toString),
        "deductedByEmployers" -> loan.deductedByEmployers.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
      ))).getOrElse("studentLoan" -> BSONNull)
  }

  def taxYearTaxRefundedOrSetOffBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.taxRefundedOrSetOff
      .map(taxRefundedOrSetOff =>
      "taxRefundedOrSetOff" -> BSONDocument(Seq(
        "amount" -> BSONDouble(taxRefundedOrSetOff.amount.doubleValue())
      ))).getOrElse("taxRefundedOrSetOff" -> BSONNull)
  }

  def taxYearChildBenefitBson(taxYearProperties: TaxYearProperties): (String, BSONValue) = {
    taxYearProperties.childBenefit
      .map(childBenefit =>
      "childBenefit" -> BSONDocument(Seq(
        "amount" -> BSONDouble(childBenefit.amount.doubleValue()),
        "numberOfChildren" -> BSONInteger(childBenefit.numberOfChildren),
        "dateBenefitStopped" -> childBenefit.dateBenefitStopped.map(x => BSONString(x.toString)).getOrElse(BSONNull)
      ))).getOrElse("childBenefit" -> BSONNull)
  }

  def findTaxYearProperties(nino: Nino, taxYear: TaxYear): Future[Option[TaxYearProperties]] = {
    for {
      optionSa <- find("nino" -> nino.nino, "taxYear" -> taxYear.taxYear).map(_.headOption)
    } yield for {
      sa <- optionSa
      taxYearProperties <- sa.taxYearProperties
    } yield taxYearProperties
  }
}
