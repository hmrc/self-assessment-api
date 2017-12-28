/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}

class BindersSpec extends UnitSpec {

  "ninoBinder.bind" should {

    "return Right when provided with a NINO passes both domain and DES validation" in {
      val nino = generateNino

      val result = Binders.ninoBinder.bind("nino", nino.nino)
      result shouldEqual Right(nino)
    }

    "return Right containing a NINO with all spaces removed when provided with a valid NINO" in {
      Binders.ninoBinder.bind("nino", "AA 99 99 99 A") shouldBe Right(Nino("AA999999A"))
    }

    "return Right containing a NINO with all letters capitalised when provided with a valid NINO" in {
      Binders.ninoBinder.bind("nino", "aa999999b") shouldBe Right(Nino("AA999999B"))
    }

    "return Left for a NINO that fails domain validation" in {
      val result = Binders.ninoBinder.bind("nino", "invalid")
      result shouldEqual Left("ERROR_NINO_INVALID")
    }
  }

  "taxYear.bind" should {

    "return Right with a TaxYear instance for a valid tax year string" in {
      val taxYear = TaxYear("2017-18")

      val result = Binders.taxYearBinder.bind("taxYear", taxYear.taxYear)
      result shouldEqual Right(taxYear)
    }

    "return Left for an invalid taxYear string" in {
      val result = Binders.taxYearBinder.bind("taxYear", "invalid")
      result shouldEqual Left("ERROR_TAX_YEAR_INVALID")
    }
  }

  "sourceType.bind" should {

    "return Right with a Source Type instance for a self-employments" in {
      SourceType.values.foreach { `type` =>
        val result = Binders.sourceTypeBinder.bind("sourceType", `type`.toString)
        result shouldEqual Right(`type`)
      }
    }

    "return Left for an invalid sourceType string" in {
      val result = Binders.sourceTypeBinder.bind("summaryType", "invalid")
      result shouldEqual Left("ERROR_INVALID_SOURCE_TYPE")
    }
  }

  "propertyType.bind" should {

    "return Right with a Property Type instance for a Property" in {
      PropertyType.values.foreach { `type` =>
        val result = Binders.propertyTypeBinder.bind("propertyType", `type`.toString)
        result shouldEqual Right(`type`)
      }
    }

    "return Left for an invalid propertyType string" in {
      val result = Binders.propertyTypeBinder.bind("propertyType", "invalid")
      result shouldEqual Left("ERROR_INVALID_PROPERTY_TYPE")
    }
  }

  "obligationQueryParamsBinder.bind" should {


    "error if \"from\" date is invalid" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("201R-02-13")))
      val oqp = result.get.left.get
      oqp shouldEqual "ERROR_INVALID_DATE_FROM"
    }

    "error if \"to\" date is invalid" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("to" -> Seq("201Z-12-13")))
      val oqp = result.get.left.get
      oqp shouldEqual "ERROR_INVALID_DATE_TO"
    }


    "error if \"from\" is greater than \"to\" date" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("2017-02-13"), "to" -> Seq("2017-01-13")))
      val oqp = result.get.left.get
      oqp shouldEqual "ERROR_INVALID_DATE_RANGE"
    }

    "bind \"from\", \"to\" dates" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("2017-02-13"), "to" -> Seq("2017-02-13")))
      val oqp = result.get.right.get
      oqp.from.get shouldEqual new LocalDate("2017-02-13")
      oqp.to.get shouldEqual new LocalDate("2017-02-13")
    }

  }

  "periodQueryParamsBinder.bind" should {

    "bind \"from\", \"to\" dates " in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("2017-02-13"), "to" -> Seq("2017-12-13")))
      val oqp = result.get.right.get
      oqp.from.get shouldEqual new LocalDate("2017-02-13")
      oqp.to.get shouldEqual new LocalDate("2017-12-13")
    }

    "error if \"from\" date is invalid" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("201R-02-13"), "to" -> Seq("2019-12-13")))
      val oqp = result.get.left.get
      oqp shouldEqual "ERROR_INVALID_DATE_FROM"
    }

    "error if \"to\" date is invalid" in {
      val result = Binders.obligationQueryParamsBinder.bind("", Map("from" -> Seq("2017-02-13"), "to" -> Seq("201Z-12-13")))
      val oqp = result.get.left.get
      oqp shouldEqual "ERROR_INVALID_DATE_TO"
    }

  }

}
