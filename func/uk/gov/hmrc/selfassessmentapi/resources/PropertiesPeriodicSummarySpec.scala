package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.JsValue
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodicSummarySpec extends BaseFunctionalSpec {

  "creating a period" should {

    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {

      s"return code 201 containing a location header containing from date and to date pointing to the newly created property period for $propertyType" in {
        given()
          .userIsAuthorisedForTheResource(nino)
          .des().properties.willBeCreatedFor(nino)
          .des().properties.periodWillBeCreatedFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(201)
          .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05".r)
      }

      s"return code 400 when provided with an invalid period for $propertyType" in {
        given()
          .userIsAuthorisedForTheResource(nino)
          .des().properties.willBeCreatedFor(nino)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(invalidPeriod(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson(propertyType))
      }

      s"return code 403 when creating an overlapping period for $propertyType" in {
        given()
          .userIsAuthorisedForTheResource(nino)
          .des().properties.willBeCreatedFor(nino)
          .des().properties.invalidPeriodFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(403)
          .bodyIsLike(Jsons.Errors.invalidPeriod)
      }

      s"return code 404 when attempting to create a period for a property that does not exist for $propertyType" in {
        given()
          .userIsAuthorisedForTheResource(nino)
          .des()
          .properties
          .periodWillBeNotBeCreatedFor(nino, propertyType)
          .when()
          .post(period(propertyType))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

    }
  }

  "retrieving all periods" ignore {
    "return code 200 with a JSON list of all FHL periods belonging to the property business" in {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2017-04-07"))

      val periodTwo = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-08"), toDate = Some("2017-04-09"))

      val expectedJson = Jsons.Properties.periodSummary(("2017-04-06", "2017-04-07"), ("2017-04-08", "2017-04-09"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne)
        .to(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo)
        .to(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
        .selectFields(_ \\ "id")
        .isLength(2)
        .matches("\\w+".r)
    }

    "return code 200 with a JSON list of all Other periods belonging to the property business" in {
      val property = Jsons.Properties()
      val periodOne = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"), toDate = Some("2017-04-07"))

      val periodTwo = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-08"), toDate = Some("2017-04-09"))

      val expectedJson = Jsons.Properties.periodSummary(("2017-04-06", "2017-04-07"), ("2017-04-08", "2017-04-09"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne)
        .to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo)
        .to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
        .selectFields(_ \\ "id")
        .isLength(2)
        .matches("\\w+".r)
    }

    "return code 200 with an empty JSON array for an FHL property business containing no periods" in {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyArray()
    }

    "return code 200 with an empty JSON array for an Other property business containing no periods" in {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyArray()
    }

    "return code 404 for an FHL property business that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 for an Other property business that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/other/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieving a single period" ignore {
    "return code 200 containing FHL period information for a period that exists" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
    }

    "return code 200 containing Other period information for a period that exists" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
    }

    "return code 404 for an FHL period that does not exist" in {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/furnished-holiday-lettings/periods/sillyid")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 for an Other period that does not exist" in {
      val property = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("%sourceLocation%/other/periods/sillyid")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "amending a single period" ignore {
    "return code 204 when updating an FHL period" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      val updatedPeriod = Jsons.Properties.fhlPeriod(rentIncome = 600,
                                                     premisesRunningCosts = 20.20,
                                                     repairsAndMaintenance = 111.25,
                                                     financialCosts = 160,
                                                     professionalFees = 1132.55,
                                                     otherCost = 50.12)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedPeriod)
        .at("%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(updatedPeriod.toString)
    }

    "return code 204 when updating an Other period" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      val updatedPeriod = Jsons.Properties.otherPeriod(rentIncome = 600,
                                                       rentIncomeTaxDeducted = 252.55,
                                                       premiumsOfLeaseGrant = Some(202.22),
                                                       reversePremiums = 22.37,
                                                       premisesRunningCosts = 20.22,
                                                       repairsAndMaintenance = 111.25,
                                                       financialCosts = 160,
                                                       professionalFees = 1132.55,
                                                       costOfServices = 510.25,
                                                       otherCost = 50.12)

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedPeriod)
        .at("%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(updatedPeriod.toString)
    }

    "return code 400 when updating an FHL period with invalid data" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      val invalidPeriod = Jsons.Properties.fhlPeriod(rentIncome = -500, financialCosts = 400.234)

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
                                                     "INVALID_MONETARY_AMOUNT" -> "/expenses/financialCosts/amount")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/furnished-holiday-lettings/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(invalidPeriod)
        .at("%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 400 when updating an Other period with invalid data" in {
      val property = Jsons.Properties()

      val period = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      val invalidPeriod = Jsons.Properties.otherPeriod(rentIncome = -500,
                                                       rentIncomeTaxDeducted = 250.55,
                                                       premiumsOfLeaseGrant = Some(-200.22),
                                                       reversePremiums = 22.35)

      val expectedJson =
        Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
                                    "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period)
        .to(s"%sourceLocation%/other/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(invalidPeriod)
        .at("%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when updating an FHL period that does not exist" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(period)
        .at("%sourceLocation%/periods/furnished-holiday-lettings/sillyid")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when updating an Other period that does not exist" in {
      val property = Jsons.Properties()
      val period = Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property)
        .to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(period)
        .at("%sourceLocation%/periods/other/sillyid")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  def period(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
        rentIncome = 500,
        premisesRunningCosts = 20.20,
        repairsAndMaintenance = 11.25,
        financialCosts = 100,
        professionalFees = 1232.55,
        otherCost = 50.22)
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        premisesRunningCosts = 20.20,
        repairsAndMaintenance = 11.25,
        financialCosts = 100,
        professionalFees = 1232.55,
        costOfServices = 500.25,
        otherCost = 50.22)
  }

  def invalidPeriod(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        financialCosts = 400.456)
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(-200.22),
        reversePremiums = 22.35)
  }

  def expectedJson(propertyType: PropertyType): String = propertyType match {
    case PropertyType.FHL =>
      Jsons.Errors.invalidRequest("INVALID_DATE" -> "/to",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/expenses/financialCosts/amount")
    case PropertyType.OTHER =>
      Jsons.Errors.invalidRequest("INVALID_DATE" -> "/to",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
        "INVALID_MONETARY_AMOUNT" -> "/incomes/premiumsOfLeaseGrant/amount")
  }
}
