/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
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

import static com.navercorp.spring.jdbc.plus.support.parametersource.converter.EnumParameterTypeConverter.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Myeonghyeon Lee
 */
class EnumParameterTypeConverterTest {
	@Test
	void enumToName() {
		// given
		EnumToNameConverter sut = EnumToNameConverter.INSTANCE;

		// when
		String actual = sut.convert(Fruit.APPLE);
		String actual2 = sut.convert(Fruit.BANANA);
		String actual3 = sut.convert(Fruit.KIWI);

		// then
		assertThat(actual).isEqualTo("APPLE");
		assertThat(actual2).isEqualTo("BANANA");
		assertThat(actual3).isEqualTo("KIWI");
	}

	@Test
	void enumToOrdinal() {
		// given
		EnumToOrdinalConverter sut = EnumToOrdinalConverter.INSTANCE;

		// when
		int actual = sut.convert(Fruit.APPLE);
		int actual2 = sut.convert(Fruit.BANANA);
		int actual3 = sut.convert(Fruit.KIWI);

		// then
		assertThat(actual).isEqualTo(0);
		assertThat(actual2).isEqualTo(1);
		assertThat(actual3).isEqualTo(2);
	}

	enum Fruit {
		APPLE, BANANA, KIWI
	}
}
