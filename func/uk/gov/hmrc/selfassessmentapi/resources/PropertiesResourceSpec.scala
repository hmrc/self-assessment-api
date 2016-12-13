package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 containing a location header when creating a uk property business" in {
      val otherProperty = Jsons.Properties()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(otherProperty).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 409 when attempting to create the same property business more than once" in {
      val property = Jsons.Properties()

      val expectedJson =
        s"""
           |{
           |  "code": "BUSINESS_ERROR",
           |  "message": "Business validation error",
           |  "errors": [
           |    {
           |      "code": "ALREADY_EXISTS",
           |      "message": "A property business already exists"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .bodyIsLike(expectedJson)
    }
  }

}
