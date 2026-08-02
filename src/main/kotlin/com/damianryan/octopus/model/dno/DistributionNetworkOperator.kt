package com.damianryan.octopus.model.dno

private const val NATIONAL_GRID = "National Grid"
private const val NORTHERN_POWERGRID = "Northern Powergrid"
private const val SCOTTISH_SOUTHERN_ELECTRICITY_NETWORKS = "Scottish & Southern Electricity Networks"
private const val SP_ENERGY_NETWORKS = "SP Energy Networks"
private const val UK_POWER_NETWORKS = "UK Power Networks"

@Suppress("unused")
enum class DistributionNetworkOperator(
    val id: Int,
    val region: String,
    val operator: String
) {
    A(10, "Eastern England", UK_POWER_NETWORKS),
    B(11, "East Midlands", NATIONAL_GRID),
    C(12, "London", UK_POWER_NETWORKS),
    D(13, "Merseyside and Northern Wales", SP_ENERGY_NETWORKS),
    E(14, "West Midlands", NATIONAL_GRID),
    F(15, "North Eastern England", NORTHERN_POWERGRID),
    G(16, "North Western England", "Electricity North West"),
    H(20, "Southern England", SCOTTISH_SOUTHERN_ELECTRICITY_NETWORKS),
    J(19, "South Eastern England", UK_POWER_NETWORKS),
    K(21, "Southern Wales", NATIONAL_GRID),
    L(22, "South Western England", NATIONAL_GRID),
    M(23, "Yorkshire", NORTHERN_POWERGRID),
    N(18, "Southern Scotland", SP_ENERGY_NETWORKS),
    P(17, "Northern Scotland", SCOTTISH_SOUTHERN_ELECTRICITY_NETWORKS);

    val gspGroupId: String
        get() = "_$name"

    override fun toString(): String {
        return "$name ($region, operator: $operator)"
    }

    companion object {
        @JvmStatic
        fun fromMpan(mpan: String): DistributionNetworkOperator? {
            val dnoCode = mpan.substring(0, 2).toInt()
            return entries.firstOrNull { it.id == dnoCode }
        }
    }
}