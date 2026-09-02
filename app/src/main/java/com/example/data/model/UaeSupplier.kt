package com.example.data.model

import androidx.compose.ui.graphics.Color

data class UaeSupplier(
    val name: String,
    val aliases: List<String>,
    val defaultTrn: String,
    val category: String,
    val brandColorHex: Long,
    val isFuel: Boolean = false
)

object UaeSuppliersRegistry {
    val suppliers = listOf(
        UaeSupplier(
            name = "ADNOC Distribution",
            aliases = listOf("adnoc", "adnoc distribution", "adnoc oasis", "adnoc service station"),
            defaultTrn = "100249581700003",
            category = "Fuel & Transport",
            brandColorHex = 0xFF004880,
            isFuel = true
        ),
        UaeSupplier(
            name = "ENOC / EPPCO",
            aliases = listOf("enoc", "eppco", "emirates national oil company", "enoc station", "zoom"),
            defaultTrn = "100018593400003",
            category = "Fuel & Transport",
            brandColorHex = 0xFF00824B,
            isFuel = true
        ),
        UaeSupplier(
            name = "Emarat",
            aliases = listOf("emarat", "emarat petrol", "emarat station", "bakeria"),
            defaultTrn = "100234901200003",
            category = "Fuel & Transport",
            brandColorHex = 0xFFE4002B,
            isFuel = true
        ),
        UaeSupplier(
            name = "Lulu Hypermarket",
            aliases = listOf("lulu", "lulu hypermarket", "lulu supermarket", "lulu group", "emke"),
            defaultTrn = "100378129000003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFF009E49
        ),
        UaeSupplier(
            name = "Carrefour UAE",
            aliases = listOf("carrefour", "majid al futtaim hypermarkets", "maf hypermarkets", "carrefour market"),
            defaultTrn = "100034871200003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFF004E9A
        ),
        UaeSupplier(
            name = "Viva Supermarket",
            aliases = listOf("viva", "viva supermarket", "viva food", "viva discount"),
            defaultTrn = "100412389100003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFFE30613
        ),
        UaeSupplier(
            name = "Spinneys UAE",
            aliases = listOf("spinneys", "spinneys dubai", "al seeri"),
            defaultTrn = "100192847100003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFF006837
        ),
        UaeSupplier(
            name = "Union Coop",
            aliases = listOf("union coop", "union cooperative society", "coop dubai"),
            defaultTrn = "100293847100003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFF2E3192
        ),
        UaeSupplier(
            name = "Al Maya Supermarket",
            aliases = listOf("al maya", "al maya supermarket", "al maya group"),
            defaultTrn = "100384729100003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFFED1C24
        ),
        UaeSupplier(
            name = "Grandiose Supermarket",
            aliases = listOf("grandiose", "grandiose supermarket", "grandiose catering"),
            defaultTrn = "100482910400003",
            category = "Groceries & Supplies",
            brandColorHex = 0xFF1E3A8A
        ),
        UaeSupplier(
            name = "Sharaf DG",
            aliases = listOf("sharaf dg", "sharaf", "sharaf retail"),
            defaultTrn = "100218492000003",
            category = "Office & Electronics",
            brandColorHex = 0xFFD97706
        ),
        UaeSupplier(
            name = "Virgin Megastore",
            aliases = listOf("virgin", "virgin megastore"),
            defaultTrn = "100184729000003",
            category = "Office & Electronics",
            brandColorHex = 0xFFCC0000
        ),
        UaeSupplier(
            name = "IKEA UAE",
            aliases = listOf("ikea", "ikea dubai", "ikea abu dhabi", "al futtaim ikea"),
            defaultTrn = "100029384700003",
            category = "Office & Equipment",
            brandColorHex = 0xFF0058A3
        ),
        UaeSupplier(
            name = "Starbucks UAE",
            aliases = listOf("starbucks", "starbucks coffee", "alshaya starbucks", "m.h. alshaya"),
            defaultTrn = "100019284700003",
            category = "Meals & Dining",
            brandColorHex = 0xFF00704A
        ),
        UaeSupplier(
            name = "Tim Hortons UAE",
            aliases = listOf("tim hortons", "tim hortons coffee", "apparel fzco"),
            defaultTrn = "100384729100003",
            category = "Meals & Dining",
            brandColorHex = 0xFFC8102E
        ),
        UaeSupplier(
            name = "Costa Coffee UAE",
            aliases = listOf("costa", "costa coffee", "emirates leisure retail"),
            defaultTrn = "100284729100003",
            category = "Meals & Dining",
            brandColorHex = 0xFF781D26
        ),
        UaeSupplier(
            name = "Aster Pharmacy",
            aliases = listOf("aster", "aster pharmacy", "aster dm healthcare"),
            defaultTrn = "100182749100003",
            category = "Healthcare",
            brandColorHex = 0xFF008272
        ),
        UaeSupplier(
            name = "Life Pharmacy",
            aliases = listOf("life pharmacy", "life healthcare"),
            defaultTrn = "100283749100003",
            category = "Healthcare",
            brandColorHex = 0xFF00A3E0
        ),
        UaeSupplier(
            name = "e& (Etisalat)",
            aliases = listOf("etisalat", "e&", "emirates telecommunications"),
            defaultTrn = "100028374900003",
            category = "Utilities & Telecom",
            brandColorHex = 0xFF719E19
        ),
        UaeSupplier(
            name = "du (EITC)",
            aliases = listOf("du", "emirates integrated telecommunications", "eitc"),
            defaultTrn = "100019283700003",
            category = "Utilities & Telecom",
            brandColorHex = 0xFF00A9CE
        ),
        UaeSupplier(
            name = "Salik / RTA Dubai",
            aliases = listOf("salik", "rta dubai", "roads and transport authority"),
            defaultTrn = "100000000000003",
            category = "Fuel & Transport",
            brandColorHex = 0xFFE30613
        ),
        UaeSupplier(
            name = "Careem / Dubai Taxi",
            aliases = listOf("careem", "dubai taxi", "careem rides", "uber uae"),
            defaultTrn = "100284920100003",
            category = "Fuel & Transport",
            brandColorHex = 0xFF00B140
        )
    )

    fun detectSupplier(rawText: String): UaeSupplier? {
        val lower = rawText.lowercase()
        for (supplier in suppliers) {
            for (alias in supplier.aliases) {
                if (lower.contains(alias)) {
                    return supplier
                }
            }
        }
        return null
    }

    val categories = listOf(
        "All Categories",
        "Fuel & Transport",
        "Groceries & Supplies",
        "Office & Electronics",
        "Meals & Dining",
        "Utilities & Telecom",
        "Healthcare",
        "Office & Equipment",
        "General"
    )
}
