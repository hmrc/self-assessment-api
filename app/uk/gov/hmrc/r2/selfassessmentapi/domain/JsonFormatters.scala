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
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.FHLExpenseType.FHLExpenseType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.FHLIncomeType.FHLIncomeType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{FHLExpenseType, FHLIncomeType}

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
  }

}
