package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.HsqlDbDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

/**
 * COPY org.springframework.data.jdbc.core.convert.NonQuotingDialect
 *
 * @author Myeonghyeon Lee
 */
public class NonQuotingDialect extends AbstractDialect implements Dialect {
	public static final NonQuotingDialect INSTANCE = new NonQuotingDialect();

	private NonQuotingDialect() {
	}

	@Override
	public LimitClause limit() {
		return HsqlDbDialect.INSTANCE.limit();
	}

	@Override
	public LockClause lock() {
		return HsqlDbDialect.INSTANCE.lock();
	}

	@Override
	public IdentifierProcessing getIdentifierProcessing() {
		return IdentifierProcessing.create(new IdentifierProcessing.Quoting(""), IdentifierProcessing.LetterCasing.AS_IS);
	}
}
