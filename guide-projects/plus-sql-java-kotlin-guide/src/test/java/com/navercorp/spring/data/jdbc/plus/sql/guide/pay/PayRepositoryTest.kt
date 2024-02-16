package com.navercorp.spring.data.jdbc.plus.sql.guide.pay

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
class PayRepositoryTest {
    @Autowired
    lateinit var sut: PayRepository

    @Test
    fun saveAll() {
        val insertedId = this.sut.saveAll((1..5).map { sample() })
            .map { it.id }

        val actual = this.sut.findAllById(insertedId)

        then(actual).hasSize(5).allSatisfy {
            then(it.payAdmissions).isNotEmpty()
        }
    }

    fun sample(): Pay = Pay.builder()
        .amount(BigDecimal.valueOf(1000L))
        .orderId(940329L)
        .payAdmissions(
            setOf(
                PayAdmission(null, null, BigDecimal.valueOf(1000L), "CARD"),
                PayAdmission(null, null, BigDecimal.valueOf(1000L), "BANK"),
                PayAdmission(null, null, BigDecimal.valueOf(1000L), "VIRTUAL_ACCOUNT")
            ),
        )
        .build()
}
