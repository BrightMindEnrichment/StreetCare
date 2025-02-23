package org.brightmindenrichment.street_care.util

object StateAbbreviation {

    private val regionsMap = mapOf(
        // United States
        "alabama" to "AL", "alaska" to "AK", "arizona" to "AZ", "arkansas" to "AR",
        "california" to "CA", "colorado" to "CO", "connecticut" to "CT", "delaware" to "DE",
        "florida" to "FL", "georgia" to "GA", "hawaii" to "HI", "idaho" to "ID",
        "illinois" to "IL", "indiana" to "IN", "iowa" to "IA", "kansas" to "KS",
        "kentucky" to "KY", "louisiana" to "LA", "maine" to "ME", "maryland" to "MD",
        "massachusetts" to "MA", "michigan" to "MI", "minnesota" to "MN", "mississippi" to "MS",
        "missouri" to "MO", "montana" to "MT", "nebraska" to "NE", "nevada" to "NV",
        "new hampshire" to "NH", "new jersey" to "NJ", "new mexico" to "NM", "new york" to "NY",
        "north carolina" to "NC", "north dakota" to "ND", "ohio" to "OH", "oklahoma" to "OK",
        "oregon" to "OR", "pennsylvania" to "PA", "rhode island" to "RI", "south carolina" to "SC",
        "south dakota" to "SD", "tennessee" to "TN", "texas" to "TX", "utah" to "UT",
        "vermont" to "VT", "virginia" to "VA", "washington" to "WA", "west virginia" to "WV",
        "wisconsin" to "WI", "wyoming" to "WY",

        // Canada
        "alberta" to "AB", "british columbia" to "BC", "manitoba" to "MB", "new brunswick" to "NB",
        "newfoundland and labrador" to "NL", "nova scotia" to "NS", "ontario" to "ON",
        "prince edward island" to "PE", "quebec" to "QC", "saskatchewan" to "SK",
        "northwest territories" to "NT", "nunavut" to "NU", "yukon" to "YT"
    )

    fun getStateOrProvinceAbbreviation(regionName: String): String {
        val normalizedRegionName = regionName.trim().lowercase()
        return regionsMap[normalizedRegionName] ?: regionName
    }
}
