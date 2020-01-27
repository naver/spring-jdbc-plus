package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EnumParameterTypeConverterTest {
	@Test
	void enumToName() {
		// given
		EnumParameterTypeConverter.EnumToNameConverter sut = EnumParameterTypeConverter.EnumToNameConverter.INSTANCE;

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
		EnumParameterTypeConverter.EnumToOrdinalConverter sut = EnumParameterTypeConverter.EnumToOrdinalConverter.INSTANCE;

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
