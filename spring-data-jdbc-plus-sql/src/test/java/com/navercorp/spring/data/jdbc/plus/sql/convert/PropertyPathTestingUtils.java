package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.PersistentPropertyPaths;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

/**
 * COPY org.springframework.data.jdbc.core.PropertyPathTestingUtils
 *
 * @author Myeonghyeon Lee
 */
public class PropertyPathTestingUtils {
	public static PersistentPropertyPath<RelationalPersistentProperty> toPath(
		String path,
		Class source,
		RelationalMappingContext context
	) {
		PersistentPropertyPaths<?, RelationalPersistentProperty> persistentPropertyPaths = context
			.findPersistentPropertyPaths(source, p -> true);

		return persistentPropertyPaths.filter(
			p -> p.toDotPath().equals(path)).stream()
			.findFirst()
			.orElse(null);
	}
}
