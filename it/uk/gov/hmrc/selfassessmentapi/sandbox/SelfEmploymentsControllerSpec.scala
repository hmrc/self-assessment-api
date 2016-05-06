package uk.gov.hmrc.selfassessmentapi.sandbox

import org.joda.time.LocalDate
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()
  val selfEmploymentId = BSONObjectID.generate.stringify

  "Sandbox Self employment" should {

    "return a 201 response with links to newly created self employment" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/sandbox/$saUtr/self-employments", Some(toJson(Some(SelfEmployment(None, "name", LocalDate.now.minusDays(1))))))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIs("application/hal+json")
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/.+".r)
    }

    "return a valid response containing an existing self employment" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/hal+json")
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/$selfEmploymentId")
    }

    "return 200 and a valid response when an existing self employment is modified" in {
      given()
        .when()
        .put(s"/sandbox/$saUtr/self-employments/$selfEmploymentId", Some(toJson(Some(SelfEmployment(None, "name", LocalDate.now.minusDays(1))))))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIs("application/hal+json")
        .bodyHasLink("self", s"/self-assessment/$saUtr/self-employments/$selfEmploymentId")
    }

    "return 204 response when an existing self employment is deleted" in {
      given()
        .when()
        .delete(s"/sandbox/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(204)
    }
  }
}
