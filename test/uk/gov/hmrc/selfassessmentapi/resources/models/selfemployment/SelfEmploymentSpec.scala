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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.domain
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, AccountingType, ErrorCode}

class SelfEmploymentSpec extends JsonSpec {
  private def selfEmploymentWithPeriods(periods: SelfEmploymentPeriod*) = {
    val periodsWithId = periods.map { period =>
      BSONObjectID.generate.stringify -> period
    }.toMap

    val id = BSONObjectID.generate
    domain.SelfEmployment(id, id.stringify, NinoGenerator().nextNino(), LocalDate.now,
      AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1)),
      AccountingType.CASH, LocalDate.now, Map.empty, periodsWithId)
  }

  "containsGap" should {
    "return false if there are no gaps in the date ranges between the existing periods and the provided period" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val newPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(2), LocalDate.now.plusDays(3), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsGap(newPeriod) shouldBe false
    }

    "return true if there is a gap in the date range between the existing periods and the provided period" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val newPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(3), LocalDate.now.plusDays(4), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsGap(newPeriod) shouldBe true
    }
  }

  "containsOverlappingPeriod" should {
    "return false if the date range of the provided period does not overlap with the date range of an existing period" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(5), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val newPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(6), LocalDate.now.plusDays(7), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsOverlappingPeriod(newPeriod) shouldBe false
    }

    "return true if the date range of the provided period does overlap with the date range of an existing period" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(5), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val abuttingPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(5), LocalDate.now.plusDays(7), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val overlappingPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(3), LocalDate.now.plusDays(4), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsOverlappingPeriod(abuttingPeriod) shouldBe true
      selfEmployment.containsOverlappingPeriod(overlappingPeriod) shouldBe true
    }
  }

  "containsMisalignedPeriod" should {
    "return true if the start date of the provided period is not equal to the start of the accounting period, when no other periods currently exist" in {
      val selfEmployment = selfEmploymentWithPeriods()
      val newPeriod = SelfEmploymentPeriod(selfEmployment.accountingPeriod.end.plusDays(1), LocalDate.now.plusDays(7), SelfEmploymentPeriodicData(Map.empty, Map.empty))

      selfEmployment.containsMisalignedPeriod(newPeriod) shouldBe true
    }

    "return true if the date of the provided period is not before or equal to the end of the accounting period, when other periods already exist" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(5), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val newPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(6), selfEmployment.accountingPeriod.end.plusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsMisalignedPeriod(newPeriod) shouldBe true
    }

    "return false if the date of the provided period is aligned with the accounting period start date when no other periods exist" in {
      val selfEmployment = selfEmploymentWithPeriods()

      val newPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsMisalignedPeriod(newPeriod) shouldBe false
    }

    "return false if the date of the provided period is aligned with the most recently added period" in {
      val existingPeriod = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(5), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val selfEmployment = selfEmploymentWithPeriods(existingPeriod)

      val newPeriod = SelfEmploymentPeriod(LocalDate.now.plusDays(6), selfEmployment.accountingPeriod.end, SelfEmploymentPeriodicData(Map.empty, Map.empty))
      selfEmployment.containsMisalignedPeriod(newPeriod) shouldBe false
    }

    "return true when the period `to` date comes after the accountingPeriod `end` date when inserting the first period" in {
      val accountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2018-03-31"))
      val period = SelfEmploymentPeriod(from = LocalDate.parse("2017-04-01"), to = LocalDate.parse("2017-04-01"), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      val se = domain.SelfEmployment(BSONObjectID.generate, "", NinoGenerator().nextNino(), LocalDate.now, accountingPeriod, AccountingType.CASH, LocalDate.now, Map.empty, Map.empty)

      se.containsMisalignedPeriod(period)
    }
  }

  "SelfEmployment JSON" should {
    "round ignore the id if it is provided by the user" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, LocalDate.now.minusDays(1))
      val expectedOutput = input.copy(id = None)

      assertJsonIs(input, expectedOutput)
    }

    "return a COMMENCEMENT_DATE_NOT_IN_THE_PAST error when using a commencement date in the future" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")), AccountingType.CASH, LocalDate.now.plusDays(1))
      assertValidationErrorWithCode(input,
        "/commencementDate", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_ACCOUNTING_PERIOD error when startDate < endDate" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01")), AccountingType.CASH, LocalDate.now.minusDays(1))
      assertValidationErrorWithCode(input,
        "/accountingPeriod", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "return a DATE_NOT_IN_THE_PAST error when proving an accounting period with a start date that is before 2017-04-01" in {
      val input = SelfEmployment(None, AccountingPeriod(LocalDate.parse("2017-03-01"), LocalDate.parse("2017-03-03")), AccountingType.CASH, LocalDate.now.minusDays(1))
      assertValidationErrorWithCode(input,
        "/accountingPeriod/start", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_VALUE error when providing an invalid accounting type" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "OHNO",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithCode[SelfEmployment](Json.parse(json), Map("/accountingType" -> ErrorCode.INVALID_VALUE))
    }

    "return a error when providing an empty commencementDate" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": ""
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json), Map("/commencementDate" -> "error.expected.jodadate.format"))
    }

    "return a error when providing an non-ISO (i.e. YYYY-MM-DD) commencementDate" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "2017-04-01",
           |    "end": "2017-04-02"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "01-01-2016"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json), Map("/commencementDate" -> "error.expected.jodadate.format"))
    }

    "return a error when providing non-ISO (i.e. YYYY-MM-DD) dates to the accountingPeriod" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "01-01-2016",
           |    "end": "02-01-2016"
           |  },
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod/start" -> "error.expected.jodadate.format",
          "/accountingPeriod/end" -> "error.expected.jodadate.format"))
    }

    "return a error when providing an empty SelfEmployment body" in {
      val json = "{}"

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod" -> "error.path.missing",
            "/accountingType" -> "error.path.missing",
            "/commencementDate" -> "error.path.missing"))
    }

    "return a error when providing an empty accountingPeriod body" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {},
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod/start" -> "error.path.missing",
            "/accountingPeriod/end" -> "error.path.missing"))
    }
  }
}
