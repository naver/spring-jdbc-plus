package com.navercorp.spring.jdbc.plus.support.parametersource;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeSqlParameterSource implements SqlParameterSource {
    private final List<SqlParameterSource> sqlParameterSources;

    public CompositeSqlParameterSource(SqlParameterSource... sqlParameterSources) {
        if (sqlParameterSources == null) {
            this.sqlParameterSources = Collections.emptyList();
        } else {
            this.sqlParameterSources = Arrays.asList(sqlParameterSources);
        }
    }

    public CompositeSqlParameterSource(List<SqlParameterSource> sqlParameterSources) {
        this.sqlParameterSources = Collections.unmodifiableList(sqlParameterSources);
    }

    @Override
    public boolean hasValue(String paramName) {
        for (SqlParameterSource each : this.sqlParameterSources) {
            if (each.hasValue(paramName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        for (SqlParameterSource each : this.sqlParameterSources) {
            if (each.hasValue(paramName)) {
                return each.getValue(paramName);
            }
        }
        throw new IllegalArgumentException("Can not find '" + paramName + "' parameter in CompositeSqlParameterSource.");
    }
}
