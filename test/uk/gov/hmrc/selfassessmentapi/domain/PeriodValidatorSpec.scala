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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, ErrorCode, Period, PeriodId}

class PeriodValidatorSpec extends UnitSpec {
  case class TestPeriod(from: LocalDate, to: LocalDate) extends Period
  case class TestPeriodValidator(periods: Map[PeriodId, TestPeriod]) extends PeriodValidator[TestPeriod]

  val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05"))

  "validatePeriod" should {

    "fail `containsOverlappingPeriod` when adding a period that overlaps with an existing period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-05"), LocalDate.parse("2018-04-06")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("")))
      }

    "pass `containsOverlappingPeriod` when adding a period that does not overlap with an existing period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-05")), accPeriod) shouldBe None
      }

    "fails `containsGap` when adding a period that creates a gap" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-07"), LocalDate.parse("2018-04-08")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("")))
      }

    "pass `containsGap` when adding a period that does not create a gap" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-05")), accPeriod) shouldBe None
      }

    "fail `containsPeriod` when adding a period that already exists" in
      new TestPeriodValidator(Map("id" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("id")))
      }

    "pass `containsPeriod` when adding a period that does not yet exist" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-03")), accPeriod) shouldBe None
      }

    "fail `containsMisalignedPeriod` when adding a period whose end date is beyond the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-06")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("")))
      }

    "pass `containsMisalignedPeriod` when adding a period whose end date is equal to the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-05")), accPeriod) shouldBe None
      }

    "pass `containsMisalignedPeriod` when adding a period whose end date is before to the end of the accounting period" in
      new TestPeriodValidator(Map("" -> TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-01")))) {
        validatePeriod(TestPeriod(LocalDate.parse("2018-04-02"), LocalDate.parse("2018-04-04")), accPeriod) shouldBe None
      }

    "fail `containsMisalignedPeriod` when adding a first period whose start date is before the start of the accounting period" in
      new TestPeriodValidator(Map.empty) {
        validatePeriod(TestPeriod(LocalDate.parse("2017-04-05"), LocalDate.parse("2017-04-15")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("")))
      }

    "fail `containsMisalignedPeriod` when adding a first period whose start date is after the start of the accounting period" in
      new TestPeriodValidator(Map.empty) {
        validatePeriod(TestPeriod(LocalDate.parse("2017-04-07"), LocalDate.parse("2017-04-08")), accPeriod) shouldBe
          Some(Error(ErrorCode.INVALID_PERIOD.toString, "Periods should be contiguous and have no gaps between one another.", Some("")))
      }

    "return None when provided with a period that passes validation" in new TestPeriodValidator(Map.empty) {
      validatePeriod(TestPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")), accPeriod) shouldBe None
    }
  }
}
