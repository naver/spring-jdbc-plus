package com.navercorp.spring.data.jdbc.plus.support.parametersource;

import java.util.regex.Pattern;

/**
 * Sanitizes the name of bind parameters, so they don't contain any illegal characters.
 *
 * @author Jens Schauder
 *
 * @since 3.0.2
 *
 * COPY: org.springframework.data.jdbc.core.convert.BindParameterNameSanitizer
 */
public abstract class BindParameterNameSanitizer {

	private static final Pattern parameterPattern = Pattern.compile("\\W");

	public static String sanitize(String rawName) {
		return parameterPattern.matcher(rawName).replaceAll("");
	}
}
