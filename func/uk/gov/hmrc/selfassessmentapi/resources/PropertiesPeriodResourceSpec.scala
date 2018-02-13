package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.models.{Amount, Period, PeriodId}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodResourceSpec extends BaseFunctionalSpec {

  "creating a period" should {

    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {

      s"return code 201 containing a location header containing from date and to date pointing to the newly created property period for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
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
          .responseContainsHeader(
            "Location",
            s"/self-assessment/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05".r)
      }

      s"return code 201 when creating an $propertyType period where the paylod contains only 'consolidatedExpenses'"  in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType, onlyConsolidated = true))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(201)
          .responseContainsHeader(
            "Location",
            s"/self-assessment/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05".r)
      }

      s"return code 201 when creating an $propertyType period where the paylod contains no expenses"  in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType, noExpenses = true))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(201)
          .responseContainsHeader(
            "Location",
            s"/self-assessment/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05".r)
      }

      s"return code 201 when creating an $propertyType period where the paylod contains only 'residentialFinancialCost'"  in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType, onlyResidential = true))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(201)
          .responseContainsHeader(
            "Location",
            s"/self-assessment/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05".r)
      }

      s"return code 400 when provided with an invalid period for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
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

      s"return code 400 when provided with an invalid period and no incomes and expenses for $propertyType" in {
        val period =
          s"""
             |{
             |  "from": "2017-05-31",
             |  "to": "2017-04-01"
             |}
         """.stripMargin

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(Json.parse(period))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.invalidRequest("NO_INCOMES_AND_EXPENSES" -> "", "INVALID_PERIOD" -> ""))
      }

      s"return code 403 when creating an overlapping period for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .overlappingPeriodFor(nino, propertyType)
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
          .bodyIsLike(Jsons.Errors.overlappingPeriod)
      }

      s"return code 403 when creating a period for $propertyType with the payload contains over 'consolidatedExpenses'" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .createWithNotAllowedConsolidatedExpenses(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType, overConsolidatedExpenses = true))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(403)
          .bodyIsLike(Jsons.Errors.notAllowedConsolidatedExpenses)
      }


      s"return code 403 when attempting to create a period that is misaligned with the accounting period for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .misalignedPeriodFor(nino, propertyType)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(misalignedPeriod(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(403)
          .bodyIsLike(Jsons.Errors.misalignedPeriod)
      }

      s"return code 400 when attempting to create a period where the paylod contains both the 'expenses' and 'consolidatedExpenses' for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .when()
          .post(Jsons.Properties())
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(bothExpenses(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.invalidRequest("BOTH_EXPENSES_SUPPLIED" -> ""))
      }

      s"return code 404 when attempting to create a period for a property that does not exist for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeNotBeCreatedFor(nino, propertyType)
          .when()
          .post(period(propertyType))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 400 when attempting to create a period for a property with an invalid 'costOfServices' field for $propertyType" in {

        val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/expenses/costOfServices/amount")

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .post(period(propertyType, costOfServices = Some(1234.567)))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsLike(expectedJson)
      }

      s"""return code 404 when attempting to create a period for a property that does not exist for $propertyType
         |and DES returns HTTP 403 INVALID_INCOME_SOURCE""".stripMargin in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeNotBeCreatedForInexistentIncomeSource(nino, propertyType)
          .when()
          .post(period(propertyType))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 500 when DES is experiencing issues for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .serverErrorFor(nino)
          .when()
          .post(period(propertyType))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when dependent systems are not available for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .serviceUnavailableFor(nino)
          .when()
          .post(period(propertyType))
          .to(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .isATeapotFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  "retrieving all periods" should {

    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {

      s"return code 200 with a JSON array of all $propertyType periods belonging to the property business" in {
        val expectedJson = Jsons.Properties.periodSummary(("2017-07-05", "2017-08-04"))
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodsWillBeReturnedFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson.toString)
          .selectFields(_ \\ "id")
          .isLength(1)
          .matches(Period.periodPattern)
      }

      s"return code 404 for an $propertyType property business containing no periods" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .noPeriodsFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 404 for an $propertyType property business that does not exist" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .doesNotExistPeriodFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 500 when we receive an unexpected JSON from DES for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .invalidPeriodsJsonFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .isATeapotFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods")
          .thenAssertThat()
          .statusIs(500)
      }

    }
  }

  "retrieving a single period" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 200 containing $propertyType period information for a period that exists" in {
        val expected = expectedBody(propertyType)
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeReturnedFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .bodyIsLike(expected)
          .bodyDoesNotHavePath[PeriodId]("id")
      }

      s"return code 404 for a $propertyType period that does not exist" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .noPeriodFor(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .isATeapotFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(500)
      }

      s"return code 500 when we receive a status code INVALID_DATE_FROM from DES for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .invalidDateFrom(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(500)
      }

      s"return code 500 when we receive a status code INVALID_DATE_TO from DES for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .invalidDateTo(nino, propertyType)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  "amending a single period" should {

    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {

      s"return code 204 when updating an $propertyType period" in {
        val updatedPeriod = period(propertyType)
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeUpdatedFor(nino, propertyType)
          .when()
          .put(updatedPeriod)
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 204 when updating an $propertyType period where the paylod contains only 'consolidatedExpenses'" in {
        val updatedPeriod = period(propertyType, onlyConsolidated = true)
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeUpdatedFor(nino, propertyType)
          .when()
          .put(updatedPeriod)
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 204 when updating an $propertyType period where the paylod contains no expenses" in {
        val updatedPeriod = period(propertyType, noExpenses = true)
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeUpdatedFor(nino, propertyType)
          .when()
          .put(updatedPeriod)
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 204 when updating an $propertyType period where the paylod contains only 'residentialFinancialCost' expenses" in {
        val updatedPeriod = period(propertyType, onlyResidential = true)
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillBeUpdatedFor(nino, propertyType)
          .when()
          .put(updatedPeriod)
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 400 when updating an $propertyType period with invalid data" in {
        val property = Jsons.Properties()

        val invalidPeriod = Jsons.Properties.fhlPeriod(rentIncome = -500, financialCosts = Some(400.234))

        val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/incomes/rentIncome/amount",
                                                       "INVALID_MONETARY_AMOUNT" -> "/expenses/financialCosts/amount")

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
          .des()
          .properties
          .invalidPeriodUpdateFor(nino, propertyType)
          .when()
          .post(property)
          .to(s"/ni/$nino/uk-properties")
          .thenAssertThat()
          .statusIs(201)
          .when()
          .post(period(propertyType))
          .to(s"%sourceLocation%/$propertyType/periods")
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

      s"return code 400 when attempting to update a period for a property with an invalid 'costOfServices' field for $propertyType" in {

        val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/expenses/costOfServices/amount")

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .put(period(propertyType, costOfServices = Some(1234.567)))
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsLike(expectedJson)
      }

      s"return code 400 when updating an $propertyType period where the paylod contains both the 'expenses' and 'consolidatedExpenses'" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
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
          .when()
          .put(bothExpensesUpdate(propertyType))
          .at("%periodLocation%")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.invalidRequest("BOTH_EXPENSES_SUPPLIED" -> ""))
      }

      s"return code 403 when updating an $propertyType period where the paylod contains over 'consolidatedExpenses'" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des()
          .properties
          .periodWillBeCreatedFor(nino, propertyType)
          .des()
          .properties
          .updateWithNotAllowedConsolidatedExpenses(nino, propertyType)
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
          .when()
          .put(period(propertyType, overConsolidatedExpenses = true))
          .at("%periodLocation%")
          .thenAssertThat()
          .statusIs(403)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.notAllowedConsolidatedExpenses)
      }

      s"return code 404 when updating an $propertyType period that does not exist" in {
        val period = Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05"))

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des()
          .properties
          .periodWillNotBeUpdatedFor(nino, propertyType)
          .when()
          .put(period)
          .at(s"/ni/$nino/uk-properties/$propertyType/periods/2017-04-06_2018-04-05")
          .thenAssertThat()
          .statusIs(404)
      }
    }
  }

  def misalignedAndOverlappingPeriod(propertyType: PropertyType) =
    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))

  def misalignedPeriod(propertyType: PropertyType) =
    period(propertyType, from = Some("2017-08-04"), to = Some("2017-09-04"))

  def bothExpenses(propertyType: PropertyType) = period(propertyType, consolidatedExpenses = Some(100.50))

  def bothExpensesUpdate(propertyType: PropertyType) =
    period(propertyType, from = None, to = None, consolidatedExpenses = Some(100.50))

  def period(propertyType: PropertyType,
             from: Option[String] = Some("2017-04-06"),
             to: Option[String] = Some("2018-04-05"),
             consolidatedExpenses: Option[Amount] = None,
             costOfServices: Option[BigDecimal] = None,
             onlyConsolidated: Boolean = false,
             onlyResidential: Boolean = false,
             noExpenses: Boolean = false,
             overConsolidatedExpenses: Boolean = false): JsValue = propertyType match {
    case PropertyType.FHL if noExpenses =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500
      )
    case PropertyType.FHL if overConsolidatedExpenses =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        consolidatedExpenses = Some(99999999999999.50)
      )
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        premisesRunningCosts = Some(100.50),
        repairsAndMaintenance = Some(100.50),
        financialCosts = Some(100),
        professionalFees = Some(100.50),
        otherCost = Some(100.50),
        costOfServices = costOfServices,
        consolidatedExpenses = consolidatedExpenses
      )
    case PropertyType.OTHER if noExpenses =>
      Jsons.Properties.consolidationPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35
      )
    case PropertyType.OTHER if onlyConsolidated =>
      Jsons.Properties.consolidationPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        consolidatedExpenses = Some(100.55)
      )
    case PropertyType.OTHER if onlyResidential =>
      Jsons.Properties.residentialPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        residentialFinancialCost = Some(100.55)
      )
    case PropertyType.OTHER if overConsolidatedExpenses =>
      Jsons.Properties.otherPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        consolidatedExpenses = Some(99999999999999.50)
      )
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(
        fromDate = from,
        toDate = to,
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        premisesRunningCosts = Some(100.50),
        repairsAndMaintenance = Some(100.50),
        financialCosts = Some(100),
        professionalFees = Some(100.50),
        costOfServices = costOfServices,
        otherCost = Some(100.50),
        residentialFinancialCost = Some(100.50),
        consolidatedExpenses = consolidatedExpenses
      )
  }

  def invalidPeriod(propertyType: PropertyType): JsValue = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties.fhlPeriod(fromDate = Some("2017-04-01"),
                                 toDate = Some("02-04-2017"),
                                 rentIncome = -500,
                                 financialCosts = Some(400.456))
    case PropertyType.OTHER =>
      Jsons.Properties.otherPeriod(
        fromDate = Some("2017-04-01"),
        toDate = Some("02-04-2017"),
        rentIncome = -500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(-200.22),
        reversePremiums = 22.35
      )
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

  def expectedBody(propertyType: PropertyType): String = propertyType match {
    case PropertyType.FHL =>
      Jsons.Properties
        .fhlPeriod(
          fromDate = Some("2017-04-05"),
          toDate = Some("2018-04-04"),
          rentIncome = 200.00,
          premisesRunningCosts = Some(200),
          repairsAndMaintenance = Some(200),
          financialCosts = Some(200),
          professionalFees = Some(200),
          costOfServices = Some(200.00),
          otherCost = Some(200)
        )
        .toString()

    case PropertyType.OTHER =>
      Jsons.Properties
        .otherPeriod(
          fromDate = Some("2017-04-05"),
          toDate = Some("2018-04-04"),
          rentIncome = 200.00,
          premiumsOfLeaseGrant = Some(200),
          reversePremiums = 200,
          premisesRunningCosts = Some(200),
          repairsAndMaintenance = Some(200),
          financialCosts = Some(200),
          professionalFees = Some(200),
          costOfServices = Some(200),
          residentialFinancialCost = Some(200),
          otherCost = Some(200)
        )
        .toString()
  }
}
