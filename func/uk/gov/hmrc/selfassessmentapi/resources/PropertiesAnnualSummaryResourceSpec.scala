package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesAnnualSummaryResourceSpec extends BaseFunctionalSpec {

  "amending annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 204 when amending annual summaries for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillBeUpdatedFor(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 400 when amending annual summaries with invalid data for $propertyType" in {
        val expectedJson = Jsons.Errors.invalidRequest(
          "INVALID_MONETARY_AMOUNT" -> "/allowances/annualInvestmentAllowance",
          "INVALID_MONETARY_AMOUNT" -> "/adjustments/privateUseAdjustment")

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .put(invalidAnnualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson.toString)
      }

      s"return code 404 when amending annual summaries for a properties business that does not exist for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillNotBeReturnedFor(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 500 when provided with an invalid Originator-Id header for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().invalidOriginatorIdFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 400 when provided with an invalid payload for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().payloadFailsValidationFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.invalidRequest)
      }

      s"return code 400 when updating properties annual summary for a non MTD year for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/2015-16")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsError("TAX_YEAR_INVALID")
      }

      s"return code 404 when attempting to update annual summaries for an invalid property type for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/silly/$taxYear")
          .thenAssertThat()
          .statusIs(404)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.notFound)
      }

      s"return code 500 when DES is experiencing problems for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().serverErrorFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when a dependent system is not responding for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().serviceUnavailableFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().isATeapotFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  "retrieving annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 200 containing annual summary information for $propertyType" in {
        val expectedJson = annualSummary(propertyType).toString()
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillBeReturnedFor(nino, propertyType, taxYear, desAnnualSummary(propertyType))
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson)
      }

      s"return code 200 containing an empty object when retrieving a non-existent annual summary for $propertyType" in {

        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().properties.noAnnualSummaryFor(nino, propertyType, taxYear)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .jsonBodyIsEmptyObject()
      }

      s"return code 404 when retrieving an annual summary for a non-existent property for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillNotBeReturnedFor(nino, propertyType, taxYear)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 400 when retrieving annual summary for a non MTD year for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/2015-16")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsError("TAX_YEAR_INVALID")
      }

      s"return code 400 when provided with an invalid Originator-Id header for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().invalidOriginatorIdFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsLike(Jsons.Errors.invalidOriginatorId)
      }

      s"return code 500 when DES is experiencing problems for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().serverErrorFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when a dependent system is not responding for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().serviceUnavailableFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .userIsFullyAuthorisedForTheResource
          .des().isATeapotFor(nino)
          .when()
          .get(s"/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  private def annualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary()
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary()
  }

  private def invalidAnnualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      businessPremisesRenovationAllowance = 500.50,
      otherCapitalAllowance = 1000.20,
      zeroEmissionsGoodsVehicleAllowance = 50.50,
      costOfReplacingDomesticItems = 150.55,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34)
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      otherCapitalAllowance = 1000.20,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34)
  }

  private def desAnnualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => DesJsons.Properties.AnnualSummary.other
    case PropertyType.FHL => DesJsons.Properties.AnnualSummary.fhl
  }
}
