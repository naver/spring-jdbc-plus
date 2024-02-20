package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("n_pay_adms")
public record PayAdmission(
	@Id
	@Nullable
	Long id,

	@Nullable
	Long payId,

	BigDecimal amount,

	String payMethodType
) {
}
