package com.navercorp.spring.data.jdbc.plus.repository.guide.product;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;

@Table("soft_delete_reviews")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class SoftDeleteReview {

	@Id
	private final Long id;
	private final Long productId;
	private final String contents;
	private final Instant createdAt;
	private final Instant lastModifiedAt;

	@SoftDeleteColumn.Boolean(valueAsDeleted = "false")
	private final boolean visible;
}
