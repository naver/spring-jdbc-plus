package com.navercorp.spring.data.jdbc.plus.repository.guide.product;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;

@Table("soft_delete_products")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class SoftDeleteProduct {

	@Id
	private final Long id;
	private final String productName;
	private final Instant createdAt;
	private final Instant lastModifiedAt;

	@MappedCollection(idColumn = "product_id")
	private final Set<SoftDeleteReview> reviews;

	@SoftDeleteColumn.Boolean(valueAsDeleted = "false")
	private final boolean visible;
}
