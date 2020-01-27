package com.navercorp.spring.data.jdbc.plus.repository.guide.shipping;

import java.util.UUID;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;

public interface ShippingRepository extends JdbcRepository<Shipping, UUID> {
}
