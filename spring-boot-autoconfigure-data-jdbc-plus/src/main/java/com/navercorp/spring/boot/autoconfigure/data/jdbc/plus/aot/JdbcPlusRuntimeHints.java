package com.navercorp.spring.boot.autoconfigure.data.jdbc.plus.aot;

import java.util.Arrays;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.data.relational.auditing.RelationalAuditingCallback;
import org.springframework.data.relational.core.mapping.event.AfterConvertCallback;
import org.springframework.data.relational.core.mapping.event.AfterDeleteCallback;
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.mapping.event.BeforeDeleteCallback;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.lang.Nullable;

import com.navercorp.spring.data.jdbc.plus.repository.support.JdbcPlusRepository;

class JdbcPlusRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {

		hints.reflection().registerTypes(
			Arrays.asList(
				TypeReference.of(JdbcPlusRepository.class),
				TypeReference.of(AfterConvertCallback.class),
				TypeReference.of(AfterDeleteCallback.class),
				TypeReference.of(AfterSaveCallback.class),
				TypeReference.of(BeforeConvertCallback.class),
				TypeReference.of(BeforeDeleteCallback.class),
				TypeReference.of(BeforeSaveCallback.class),
				TypeReference.of(RelationalAuditingCallback.class)
			),
			builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_PUBLIC_METHODS));

		hints.proxies()
			.registerJdkProxy(
				TypeReference.of("org.springframework.data.jdbc.core.convert.RelationResolver"),
				TypeReference.of("org.springframework.aop.SpringProxy"),
				TypeReference.of("org.springframework.aop.framework.Advised"),
				TypeReference.of("org.springframework.core.DecoratingProxy")
			);
	}
}
