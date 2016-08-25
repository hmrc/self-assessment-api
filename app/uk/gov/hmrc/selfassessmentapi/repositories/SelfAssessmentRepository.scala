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
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDouble, BSONElement, BSONNull, BSONObjectID, BSONString, Producer}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.domain.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.domain.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.domain.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.domain.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.domain.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.domain.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.domain.{TaxYear, TaxYearProperties, TaxYearPropertyType}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoSelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object SelfAssessmentRepository extends MongoDbConnection {
  private lazy val repository = new SelfAssessmentMongoRepository(featureSwitch)

  private lazy val featureSwitch = FeatureSwitch(AppContext.featureSwitch)

  def apply() = repository
}

class SelfAssessmentMongoRepository(featureSwitch: FeatureSwitch)(implicit mongo: () => DB)
  extends ReactiveRepository[MongoSelfAssessment, BSONObjectID](
    "selfAssessments",
    mongo,
    domainFormat = MongoSelfAssessment.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with AtomicUpdate[MongoSelfAssessment] {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("sa_utr_taxyear"), unique = true),
    Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("sa_last_modified"), unique = false))


  def touch(saUtr: SaUtr, taxYear: TaxYear) = {

    for {
      result <- atomicUpsert(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString)),
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


  def findBy(saUtr: SaUtr, taxYear: TaxYear): Future[Option[MongoSelfAssessment]] = {
    find(
      "saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString)
    ).map(_.headOption)
  }

  def findOlderThan(lastModified: DateTime): Future[Seq[MongoSelfAssessment]] = {
    find(
      "lastModifiedDateTime" -> BSONDocument("$lt" -> BSONDateTime(lastModified.getMillis))
    )
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear): Future[Boolean] = {
    for (option <- remove("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield option.n > 0
  }

  def isInsertion(suppliedId: BSONObjectID, returned: MongoSelfAssessment): Boolean = suppliedId.equals(returned.id)

  def updateTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear, taxYearProperties: TaxYearProperties): Future[Unit] = {
    val now = DateTime.now(DateTimeZone.UTC)

    val pensionContributionModifiers:BSONDocument =
      taxYearProperties.pensionContributions
        .map(pensionContributions =>
            BSONDocument(
              lastModifiedDateTimeModfier(now),
              "taxYearProperties" -> BSONDocument(
                "pensionContributions" -> BSONDocument(
                  Seq(
                    "ukRegisteredPension" -> pensionContributions.ukRegisteredPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                    "retirementAnnuity" -> pensionContributions.retirementAnnuity.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                    "employerScheme" -> pensionContributions.employerScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                    "overseasPension" -> pensionContributions.overseasPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
                    "pensionSaving" -> BSONDocument(Seq(
                      "excessOfAnnualAllowance" -> pensionContributions.pensionSaving.map(_.excessOfAnnualAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull),
                      "taxPaidByPensionScheme" -> pensionContributions.pensionSaving.map(_.taxPaidByPensionScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull)
                    )))))))
        .getOrElse(BSONDocument(
          lastModifiedDateTimeModfier(now),
          "pensionContributions" -> BSONNull))
    for {
      result <- atomicUpsert(
        BSONDocument("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear),
        BSONDocument(
          setOnInsert(now),
          "$set" -> pensionContributionModifiers
        ))
    } yield ()
  }

  def findTaxYearProperties(saUtr: SaUtr, taxYear: TaxYear): Future[Option[TaxYearProperties]] = {
    for {
      optionSa <- find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear).map(_.headOption)
    } yield for {
      sa <- optionSa
      taxYearProperties <- sa.taxYearProperties
    } yield createSwitchedProperties(taxYearProperties)
  }

  private[this] def createSwitchedProperties(taxYearProperties: TaxYearProperties): TaxYearProperties = {
    val pensionContribution = if (featureIsEnabled(PensionContributions)) taxYearProperties.pensionContributions else None
    val charitableGiving = if (featureIsEnabled(CharitableGivings)) taxYearProperties.charitableGivings else None
    val blindPerson = if (featureIsEnabled(BlindPersons)) taxYearProperties.blindPerson else None
    val studentLoan = if (featureIsEnabled(StudentLoans)) taxYearProperties.studentLoan else None
    val taxRefundedOrSetOff = if (featureIsEnabled(TaxRefundedOrSetOffs)) taxYearProperties.taxRefundedOrSetOff else None
    val childBenefit = if (featureIsEnabled(ChildBenefits)) taxYearProperties.childBenefit else None

    TaxYearProperties(taxYearProperties.id, pensionContribution, charitableGiving, blindPerson, studentLoan, taxRefundedOrSetOff, childBenefit)
  }

  private[this] def featureIsEnabled(source: TaxYearPropertyType): Boolean = featureSwitch.isEnabled(source)
}
