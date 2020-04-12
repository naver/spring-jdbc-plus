package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.HsqlDbDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

/**
 * COPY {@link org.springframework.data.jdbc.core.convert.NonQuotingDialect}
 */
public class NonQuotingDialect extends AbstractDialect implements Dialect {
	public static final NonQuotingDialect INSTANCE = new NonQuotingDialect();

	private NonQuotingDialect() {}

	@Override
	public LimitClause limit() {
		return HsqlDbDialect.INSTANCE.limit();
	}

	@Override
	public IdentifierProcessing getIdentifierProcessing() {
		return IdentifierProcessing.create(new IdentifierProcessing.Quoting(""), IdentifierProcessing.LetterCasing.AS_IS);
	}
}
