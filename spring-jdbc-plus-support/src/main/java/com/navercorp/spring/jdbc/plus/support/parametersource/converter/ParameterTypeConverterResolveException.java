/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import org.springframework.core.convert.ConversionException;

/**
 * The type Parameter type converter resolve exception.
 *
 * @author Myeonghyeon Lee
 */
public class ParameterTypeConverterResolveException extends ConversionException {
	/**
	 * Instantiates a new Parameter type converter resolve exception.
	 *
	 * @param msg the msg
	 */
	public ParameterTypeConverterResolveException(String msg) {
		super(msg);
	}
}
