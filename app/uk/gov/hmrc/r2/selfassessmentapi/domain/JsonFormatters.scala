/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.domain

import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue}
import uk.gov.hmrc.r2.selfassessmentapi.models.TaxYear
import uk.gov.hmrc.r2.selfassessmentapi.models.banks.BankAnnualSummary
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.FHLExpenseType.FHLExpenseType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.FHLIncomeType.FHLIncomeType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{FHLExpenseType, FHLIncomeType, FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary}

/**
  * Provides a suite of JSON formats for objects used throughout the codebase.
  */
object JsonFormatters {

  sealed abstract class MapEnumFormat[K <: Enumeration#Value, V: Format] extends Format[Map[K, V]] {
    override def writes(o: Map[K, V]): JsValue = {
      val mapped = o.map {
        case (k, v) => k.toString -> v
      }
      play.api.libs.json.Writes.mapWrites[V].writes(mapped)
    }

    override def reads(json: JsValue): JsResult[Map[K, V]]
  }

  object SelfEmploymentFormatters {

    import uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
    import uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment.BalancingChargeType.BalancingChargeType
    import uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment.BalancingChargeType

    implicit def balancingChargeTypeFormat[V: Format]: MapEnumFormat[BalancingChargeType, V] = new MapEnumFormat[BalancingChargeType, V] {
      override def reads(json: JsValue): JsResult[Map[BalancingChargeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => BalancingChargeType.withName(k) -> v
          })
        }
      }
    }

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, SelfEmploymentAnnualSummary]] = new Format[Map[TaxYear, SelfEmploymentAnnualSummary]] {
      override def writes(o: Map[TaxYear, SelfEmploymentAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[SelfEmploymentAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, SelfEmploymentAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[SelfEmploymentAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

  object PropertiesFormatters {

    import uk.gov.hmrc.r2.selfassessmentapi.models.properties.ExpenseType.ExpenseType
    import uk.gov.hmrc.r2.selfassessmentapi.models.properties.IncomeType.IncomeType
    import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{ ExpenseType, IncomeType}

    implicit def incomeTypeFormat[V: Format]: MapEnumFormat[IncomeType, V] = new MapEnumFormat[IncomeType, V] {
      override def reads(json: JsValue): JsResult[Map[IncomeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => IncomeType.withName(k) -> v
          })
        }
      }
    }

    implicit def fhlIncomeTypeFormat[V: Format]: MapEnumFormat[FHLIncomeType, V] = new MapEnumFormat[FHLIncomeType, V] {
      override def reads(json: JsValue): JsResult[Map[FHLIncomeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => FHLIncomeType.withName(k) -> v
          })
        }
      }
    }

    implicit def expenseTypeFormat[V: Format]: MapEnumFormat[ExpenseType, V] = new MapEnumFormat[ExpenseType, V] {
      override def reads(json: JsValue): JsResult[Map[ExpenseType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => ExpenseType.withName(k) -> v
          })
        }
      }
    }

    implicit def fhlExpenseTypeFormat[V: Format]: MapEnumFormat[FHLExpenseType, V] = new MapEnumFormat[FHLExpenseType, V] {
      override def reads(json: JsValue): JsResult[Map[FHLExpenseType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => FHLExpenseType.withName(k) -> v
          })
        }
      }
    }

    implicit val annualSummaryOtherMapFormat: Format[Map[TaxYear, OtherPropertiesAnnualSummary]] = new Format[Map[TaxYear, OtherPropertiesAnnualSummary]] {
      override def writes(o: Map[TaxYear, OtherPropertiesAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[OtherPropertiesAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, OtherPropertiesAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[OtherPropertiesAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }

    implicit val annualSummaryFHLMapFormat: Format[Map[TaxYear, FHLPropertiesAnnualSummary]] = new Format[Map[TaxYear, FHLPropertiesAnnualSummary]] {
      override def writes(o: Map[TaxYear, FHLPropertiesAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[FHLPropertiesAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, FHLPropertiesAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[FHLPropertiesAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

  object DividendsFormatters {
    import uk.gov.hmrc.r2.selfassessmentapi.models.dividends

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, dividends.Dividends]] = new Format[Map[TaxYear, dividends.Dividends]] {
      override def writes(o: Map[TaxYear, dividends.Dividends]): JsValue = {
        play.api.libs.json.Writes.mapWrites[dividends.Dividends].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, dividends.Dividends]] = {
        play.api.libs.json.Reads.mapReads[dividends.Dividends].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

  object BankFormatters {

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, BankAnnualSummary]] = new Format[Map[TaxYear, BankAnnualSummary]] {
      override def writes(o: Map[TaxYear, BankAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[BankAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, BankAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[BankAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

}
