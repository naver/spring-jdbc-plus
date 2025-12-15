package com.navercorp.spring.data.jdbc.plus.sql.guide.shipping;

import org.springframework.data.repository.CrudRepository;

import com.navercorp.spring.data.jdbc.plus.sql.guide.support.InsertRepository;

public interface ShippingRepository extends CrudRepository<Shipping, String>, InsertRepository<Shipping> {
}
