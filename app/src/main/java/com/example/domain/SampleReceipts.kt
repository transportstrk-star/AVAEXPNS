package com.example.domain

import com.example.data.local.ReceiptEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SampleReceiptItem(
    val title: String,
    val subtitle: String,
    val supplier: String,
    val rawOcrText: String,
    val entity: ReceiptEntity
)

object SampleReceiptsData {
    val sampleList = listOf(
        SampleReceiptItem(
            title = "ADNOC Service Station",
            subtitle = "Fuel Super 98 (47.5 Litres)",
            supplier = "ADNOC Distribution",
            rawOcrText = """
                ADNOC DISTRIBUTION PJSC
                TAX INVOICE
                TRN: 100249581700003
                Station #742 - Sheikh Zayed Rd, Dubai
                Date: 2026-08-30 08:45:12
                Invoice No: AD-982341
                --------------------------------
                SUPER 98 (47.50 L @ 3.42)
                Total Excl. VAT (Net): 154.52 AED
                VAT 5.00%: 7.73 AED
                TOTAL INCL. VAT: 162.25 AED
                Payment: VISA **** 4921
                Status: APPROVED
                Thank you for choosing ADNOC!
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "AD-982341",
                date = "2026-08-30",
                supplier = "ADNOC Distribution",
                trn = "100249581700003",
                description = "Fuel Super 98 (47.50L)",
                totalAed = 162.25,
                vatAed = 7.73,
                netAed = 154.52,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/adnoc_receipt_84920.jpg",
                ocrPreview = "ADNOC DISTRIBUTION PJSC\nTRN: 100249581700003\nSUPER 98 (47.50 L)\nTOTAL: 162.25 AED",
                status = "Verified",
                category = "Fuel & Transport",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Company vehicle monthly refuel"
            )
        ),
        SampleReceiptItem(
            title = "Lulu Hypermarket",
            subtitle = "Office Pantry & Cleaning Supplies",
            supplier = "Lulu Hypermarket",
            rawOcrText = """
                LULU HYPERMARKET AL BARSHA
                TAX INVOICE
                TRN: 100378129000003
                Date: 2026-08-28 16:20:00
                Invoice No: LU-2026-4491
                --------------------------------
                Nescafé Gold 200g          38.50
                Almarai Milk 2L x 3        24.00
                Tissue Box Pack 5s         28.75
                Dettol Disinfectant 1L     21.00
                Tea Bags English Bfast     18.50
                --------------------------------
                Net Amount:               124.52 AED
                VAT @ 5%:                   6.23 AED
                TOTAL AMOUNT:             130.75 AED
                Cash Tendered: 150.00 | Change: 19.25
                THANK YOU VISIT AGAIN
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "LU-2026-4491",
                date = "2026-08-28",
                supplier = "Lulu Hypermarket",
                trn = "100378129000003",
                description = "Office Pantry & Cleaning Supplies",
                totalAed = 130.75,
                vatAed = 6.23,
                netAed = 124.52,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/lulu_invoice_4491.jpg",
                ocrPreview = "LULU HYPERMARKET AL BARSHA\nTRN: 100378129000003\nINV: LU-2026-4491\nTOTAL: 130.75 AED",
                status = "Verified",
                category = "Groceries & Supplies",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Weekly office refreshments"
            )
        ),
        SampleReceiptItem(
            title = "Carrefour UAE",
            subtitle = "IT Cables & Printer Paper",
            supplier = "Carrefour UAE",
            rawOcrText = """
                CARREFOUR - MAJID AL FUTTAIM
                MALL OF THE EMIRATES - DUBAI
                TAX INVOICE
                TRN: 100034871200003
                Invoice #: CR-883019
                Date: 2026-08-25 11:15:30
                --------------------------------
                A4 Copier Paper 5 Reams     85.00
                HDMI 4K Cable 2m           45.00
                USB-C Multiport Hub        165.00
                Logitech Wireless Mouse     95.00
                --------------------------------
                Gross Total:              390.00 AED
                VAT 5% (Included):         18.57 AED
                Net Amount:               371.43 AED
                TOTAL PAYABLE:            390.00 AED
                Card: MASTERCARD *** 8812
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "CR-883019",
                date = "2026-08-25",
                supplier = "Carrefour UAE",
                trn = "100034871200003",
                description = "IT Cables & Printer Paper",
                totalAed = 390.00,
                vatAed = 18.57,
                netAed = 371.43,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/carrefour_883019.jpg",
                ocrPreview = "CARREFOUR MAF\nTRN: 100034871200003\nCR-883019\nTOTAL: 390.00 AED (VAT 18.57)",
                status = "Exported",
                category = "Office & Electronics",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Workstation upgrades"
            )
        ),
        SampleReceiptItem(
            title = "ENOC / EPPCO",
            subtitle = "Special 95 Fuel + Zoom Snack",
            supplier = "ENOC / EPPCO",
            rawOcrText = """
                ENOC SERVICE STATION 1082
                EMIRATES NATIONAL OIL CO.
                TAX INVOICE
                TRN: 100018593400003
                Date: 2026-08-20 19:40:11
                Bill No: EN-551029
                --------------------------------
                Special 95 Fuel (35.00L)   115.50
                ZOOM - Espresso & Croissant  22.00
                --------------------------------
                TOTAL EXCL. VAT:          130.95 AED
                VAT (5%):                   6.55 AED
                TOTAL INCL. VAT:          137.50 AED
                Paid by Apple Pay
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "EN-551029",
                date = "2026-08-20",
                supplier = "ENOC / EPPCO",
                trn = "100018593400003",
                description = "Special 95 Fuel + Zoom Snack",
                totalAed = 137.50,
                vatAed = 6.55,
                netAed = 130.95,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/enoc_551029.jpg",
                ocrPreview = "ENOC STATION 1082\nTRN: 100018593400003\nTOTAL INCL. VAT: 137.50 AED",
                status = "Verified",
                category = "Fuel & Transport",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Field visit travel"
            )
        ),
        SampleReceiptItem(
            title = "Sharaf DG",
            subtitle = "External SSD & Monitor Stand",
            supplier = "Sharaf DG",
            rawOcrText = """
                SHARAF DG LLC
                DUBAI MALL, FINANCIAL CENTER RD
                TAX INVOICE
                TRN: 100218492000003
                Date: 2026-08-15 14:10:05
                Invoice: SDG-771204
                --------------------------------
                Samsung T7 1TB Portable SSD  420.00
                Ergonomic Dual Monitor Arm   260.00
                --------------------------------
                Subtotal (Net):             647.62 AED
                VAT 5%:                      32.38 AED
                GRAND TOTAL:                680.00 AED
                Paid with Corporate Card
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "SDG-771204",
                date = "2026-08-15",
                supplier = "Sharaf DG",
                trn = "100218492000003",
                description = "External SSD & Monitor Stand",
                totalAed = 680.00,
                vatAed = 32.38,
                netAed = 647.62,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/sharafdg_771204.jpg",
                ocrPreview = "SHARAF DG LLC\nTRN: 100218492000003\nSDG-771204\nTOTAL: 680.00 AED",
                status = "Verified",
                category = "Office & Electronics",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Data backup drive"
            )
        ),
        SampleReceiptItem(
            title = "Starbucks UAE",
            subtitle = "Client Coffee & Pastries Meeting",
            supplier = "Starbucks UAE",
            rawOcrText = """
                STARBUCKS COFFEE - ALSHAYA TRADING CO.
                CITY CENTRE DEIRA
                TAX INVOICE
                TRN: 100019284700003
                Date: 2026-08-10 10:30:22
                Order: SB-330129
                --------------------------------
                2x Grande Caramel Macchiato  52.00
                1x Iced Shaken Espresso      26.00
                2x Butter Croissant          30.00
                --------------------------------
                Subtotal:                   102.86 AED
                VAT @ 5%:                     5.14 AED
                TOTAL:                      108.00 AED
                Payment: Contactless Card
            """.trimIndent(),
            entity = ReceiptEntity(
                invoiceNumber = "SB-330129",
                date = "2026-08-10",
                supplier = "Starbucks UAE",
                trn = "100019284700003",
                description = "Client Coffee & Pastries Meeting",
                totalAed = 108.00,
                vatAed = 5.14,
                netAed = 102.86,
                vatRate = 5.0,
                imageUrl = "https://drive.google.com/file/d/starbucks_330129.jpg",
                ocrPreview = "STARBUCKS - ALSHAYA\nTRN: 100019284700003\nSB-330129\nTOTAL: 108.00 AED",
                status = "Pending Review",
                category = "Meals & Dining",
                ocrEngineUsed = "🤖 Dual OCR (Vision + Local)",
                notes = "Client intake discussion"
            )
        )
    )
}
