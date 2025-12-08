package com.navercorp.spring.data.jdbc.plus.sql.guide.pay

import com.navercorp.spring.data.jdbc.plus.sql.guide.test.ArbitrarySpec.fixtureMonkey
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PayRepositoryTest {
    @Autowired
    lateinit var sut: PayRepository

    @Test
    fun saveAll() {
        // Given
        val samples = fixtureMonkey.giveMe(Pay::class.java, 5)
            .let { this.sut.saveAll(it) }

        // When
        val insertedIds = this.sut.saveAll(samples).mapNotNull { it.id }
        val actual = this.sut.findAllById(insertedIds)

        then(actual).hasSize(5).allSatisfy {
            then(it.payAdmissions).isNotEmpty()
        }
    }
}
