package com.damianryan.octopus.model

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AgreementTest {

    @ParameterizedTest
    @MethodSource("electricityAgreements")
    fun `electricity agreement has the expected fuel type`(tariffCode: String) {
        assertThat(Agreement(tariffCode, VALID_FROM).fuelType).isEqualTo("E")
    }

    @ParameterizedTest
    @MethodSource("gasAgreements")
    fun `gas agreement has the expected fuel type`(tariffCode: String) {
        assertThat(Agreement(tariffCode, VALID_FROM).fuelType).isEqualTo("G")
    }

    @ParameterizedTest
    @MethodSource("electricityAgreements")
    fun `electricity agreement has the expected register type`(tariffCode: String) {
        assertThat(Agreement(tariffCode, VALID_FROM).registerType).isEqualTo("1R")
    }

    @ParameterizedTest
    @MethodSource("electricityAgreements")
    fun `electricity agreement has the expected electricity region`(tariffCode: String) {
        assertThat(Agreement(tariffCode, VALID_FROM).electricityRegion).isEqualTo("_A")
    }

    @ParameterizedTest
    @MethodSource("products")
    fun `agreement has the expected product code`(tariffCode: String, expectedProductCode: String) {
        assertThat(Agreement(tariffCode, VALID_FROM).productCode).isEqualTo(expectedProductCode)
    }

    companion object {
        val VALID_FROM: Instant = Instant.now()

        @JvmStatic
        fun electricityAgreements(): List<String> =
            listOf(
                "E-1R-GO-FIX-12M-26-06-30-A",
                "E-1R-VAR-22-11-01-A",
                "E-1R-GO-VAR-22-10-14-A",
                "E-1R-GO-22-03-29-A",
                "E-1R-GO-21-05-13-A",
                "E-1R-OE-FIX-24M-21-05-29-A")

        @JvmStatic
        fun products() =
            listOf(
                Arguments.of("E-1R-GO-FIX-12M-26-06-30-A", "GO-FIX-12M-26-06-30"),
                Arguments.of("E-1R-OE-FIX-24M-21-05-29-A", "OE-FIX-24M-21-05-29"),
                Arguments.of("G-1R-OE-LOYAL-FIX-16M-25-02-12-A", "OE-LOYAL-FIX-16M-25-02-12"))

        @JvmStatic
        fun gasAgreements(): List<String> =
            listOf("G-1R-VAR-22-11-01-A", "G-1R-OE-LOYAL-FIX-16M-25-02-12-A", "G-1R-OE-FIX-24M-21-05-29-A")
    }
}
