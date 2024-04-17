package com.navercorp.spring.data.jdbc.plus.repository.guide.product;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Table("plain_products")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class PlainProductWithSoftDeleteReview {

	@Id
	private final Long id;
	private final String productName;
	private final Instant createdAt;
	private final Instant lastModifiedAt;

	@MappedCollection(idColumn = "product_id")
	private final Set<SoftDeleteReview> reviews;
}
