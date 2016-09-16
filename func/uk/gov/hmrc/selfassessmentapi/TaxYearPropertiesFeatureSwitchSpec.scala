package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class SwitchPensionContributionOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.pensionContributions.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if pension contribution is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	},
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchPensionContributionOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.pensionContributions.enabled" -> false,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if pension contribution is turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"pensionContributions": {
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           | 	},
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}

class SwitchCharitableGivingsOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.charitableGivings.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if charitable givings are turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"charitableGivings": {
           |     "giftAidPayments": {
           |       "totalInTaxYear": 10000.0,
           |       "oneOff": 5000.0,
           |       "toNonUkCharities": 1000.0,
           |       "carriedBackToPreviousTaxYear": 1000.0,
           |       "carriedFromNextTaxYear": 2000.0
           |     },
           |     "sharesSecurities": {
           |       "totalInTaxYear": 2000.0,
           |       "toNonUkCharities": 500.0
           |     },
           |     "landProperties":  {
           |       "totalInTaxYear": 4000.0,
           |       "toNonUkCharities": 3000.0
           |     }
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchCharitableGivingsOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.charitableGivings.enabled" -> false,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if charitable givings are turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"charitableGivings": {
           |     "giftAidPayments": {
           |       "totalInTaxYear": 10000.0,
           |       "oneOff": 5000.0,
           |       "toNonUkCharities": 1000.0,
           |       "carriedBackToPreviousTaxYear": 1000.0,
           |       "carriedFromNextTaxYear": 2000.0
           |     },
           |     "sharesSecurities": {
           |       "totalInTaxYear": 2000.0,
           |       "toNonUkCharities": 500.0
           |     },
           |     "landProperties":  {
           |       "totalInTaxYear": 4000.0,
           |       "toNonUkCharities": 3000.0
           |     }
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}

class SwitchBlindPersonOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.blindPerson.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if blind person is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"blindPerson": {
           | 		"country": "Wales",
           | 		"registrationAuthority": "Registrar",
           | 		"spouseSurplusAllowance": 2000.05,
           | 		"wantSpouseToUseSurplusAllowance": true
           | 	},
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchBlindPersonOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.blindPerson.enabled" -> false,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if blind person is turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"blindPerson": {
           | 		"country": "Wales",
           | 		"registrationAuthority": "Registrar",
           | 		"spouseSurplusAllowance": 2000.05,
           | 		"wantSpouseToUseSurplusAllowance": true
           | 	},
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}

class SwitchStudentLoanOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.studentLoan.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if student loan is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"studentLoan": {
           |     "planType": "Plan1",
           |     "deductedByEmployers": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchStudentLoanOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.studentLoan.enabled" -> false,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if student loan is turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"studentLoan": {
           |     "planType": "Plan1",
           |     "deductedByEmployers": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}

class SwitchTaxRefundedOrSetOffOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.taxRefundedOrSetOff.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if student loan is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"taxRefundedOrSetOff": {
           |     "amount": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchTaxRefundedOrSetOffOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.taxRefundedOrSetOff.enabled" -> false,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if student loan is turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"taxRefundedOrSetOff": {
           |     "amount": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}

class SwitchChildBenefitOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.taxRefundedOrSetOff.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> true))

  "if student loan is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"taxRefundedOrSetOff": {
           |     "amount": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .statusIs(200)
    }
  }
}

class SwitchChildBenefitOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxYearProperties.taxRefundedOrSetOff.enabled" -> true,
    "Test.feature-switch.taxYearProperties.childBenefit.enabled" -> false))

  "if student loan is turned off then tax year resource" should {
    "return 400 bad request with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 	"taxRefundedOrSetOff": {
           |     "amount": 2000.00
           |   },
           |  "childBenefit": {
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-05"
           |  }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear", Some(payload))
        .thenAssertThat()
        .isBadRequest
    }
  }
}
