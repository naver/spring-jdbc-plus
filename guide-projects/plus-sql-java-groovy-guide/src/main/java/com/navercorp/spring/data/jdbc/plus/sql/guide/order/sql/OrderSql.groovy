/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.Order
import com.navercorp.spring.data.jdbc.plus.sql.guide.order.OrderCriteria
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

/**
 * @author Myeonghyeon Lee
 */
class OrderSql extends SqlGeneratorSupport {

    String selectByPurchaserNo() {
        """
        SELECT ${sql.columns(Order)} 
        FROM ${sql.tables(Order)}
        WHERE purchaser_no = :purchaserNo
        """
    }

    String search(OrderCriteria criteria) {
        """
        SELECT ${sql.columns(Order)} 
        FROM ${sql.tables(Order)}
        WHERE purchaser_no = :purchaserNo
        AND status = :status
        ${
            switch (criteria.sortBy) {
                case OrderCriteria.OrderSort.ID:
                    return "ORDER BY id"
                case OrderCriteria.OrderSort.PRICE:
                    return "ORDER BY price"
                default:
                    return ""
            }
        }
        """
    }

    String countByPurchaserNo() {
        """
        SELECT count(*)
        FROM ${sql.tables(Order)}
        WHERE purchaser_no = :purchaserNo
        """
    }
}
