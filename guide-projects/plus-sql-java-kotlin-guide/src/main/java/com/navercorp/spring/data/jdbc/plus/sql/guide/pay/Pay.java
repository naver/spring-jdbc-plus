package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.math.BigDecimal;
import java.util.Set;

import javax.annotation.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("n_pay")
public record Pay(
	@Id
	@Nullable
	Long id,

	long orderId,

	BigDecimal amount,

	@MappedCollection(idColumn = "pay_id")
	Set<PayAdmission> payAdmissions
) {
}
