package com.damianryan.octopus.model.dno

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class DistributionNetworkOperatorTest {

    @ParameterizedTest
    @MethodSource("dnos")
    fun `should return correct DNO for MPAN`(mpan: String, expectedDno: DistributionNetworkOperator) {
        val dno = DistributionNetworkOperator.fromMpan(mpan)
        assertThat(dno).isEqualTo(expectedDno)
    }

    @ParameterizedTest
    @EnumSource(DistributionNetworkOperator::class)
    fun `should return correct GSP group id for value`(dno: DistributionNetworkOperator) {
        assertThat(dno.gspGroupId).isEqualTo("_${dno.name}")
    }

    companion object {
        @JvmStatic fun dnos() =
            listOf(
                Arguments.of("1014469335569", DistributionNetworkOperator.A),
                Arguments.of("1114469335568", DistributionNetworkOperator.B),
                Arguments.of("1214469335567", DistributionNetworkOperator.C),
                Arguments.of("1314469335566", DistributionNetworkOperator.D),
                Arguments.of("1414469335565", DistributionNetworkOperator.E),
                Arguments.of("1514469335564", DistributionNetworkOperator.F),
                Arguments.of("1614469335563", DistributionNetworkOperator.G),
                Arguments.of("1714469335562", DistributionNetworkOperator.P),
                Arguments.of("1814469335561", DistributionNetworkOperator.N),
                Arguments.of("1914469335560", DistributionNetworkOperator.J),
                Arguments.of("2014469335569", DistributionNetworkOperator.H),
                Arguments.of("2114469335568", DistributionNetworkOperator.K),
                Arguments.of("2214469335567", DistributionNetworkOperator.L),
                Arguments.of("2314469335566", DistributionNetworkOperator.M),
            )
    }
}