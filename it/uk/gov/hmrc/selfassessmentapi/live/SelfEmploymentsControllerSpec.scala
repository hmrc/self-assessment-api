package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain.SelfEmployment
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsControllerSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()
  val selfEmploymentId = BSONObjectID.generate.stringify

  "Create self-employment" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/self-employments", Some(toJson(SelfEmployment(None, "name", LocalDate.now.minusDays(1)))))
        .thenAssertThat()
        .statusIs(501)
        .body(_ \ "code").is(ErrorNotImplemented.errorCode)
        .body(_ \ "message").is(ErrorNotImplemented.message)
    }
  }

  "Find self-employment by id" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(501)
        .body(_ \ "code").is(ErrorNotImplemented.errorCode)
        .body(_ \ "message").is(ErrorNotImplemented.message)
    }
  }

  "Update self-employment" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/self-employments/$selfEmploymentId", Some(toJson(SelfEmployment(None, "name", LocalDate.now.minusDays(1)))))
        .thenAssertThat()
        .statusIs(501)
        .body(_ \ "code").is(ErrorNotImplemented.errorCode)
        .body(_ \ "message").is(ErrorNotImplemented.message)
    }
  }

  "Delete self-employment" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(501)
        .body(_ \ "code").is(ErrorNotImplemented.errorCode)
        .body(_ \ "message").is(ErrorNotImplemented.message)
    }
  }
}
