package com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql


import com.navercorp.spring.data.jdbc.plus.sql.guide.order.OrderCriteria
import com.navercorp.spring.data.jdbc.plus.sql.guide.order.Order
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

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
