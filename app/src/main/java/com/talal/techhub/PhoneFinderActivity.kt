package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Product

class PhoneFinderActivity : AppCompatActivity() {

    private val phones = mutableListOf<Product>()

    private lateinit var searchBox: EditText
    private lateinit var brandBox: EditText
    private lateinit var chipsetBox: EditText
    private lateinit var displayBox: EditText
    private lateinit var refreshBox: EditText
    private lateinit var ramBox: EditText
    private lateinit var batteryBox: EditText
    private lateinit var ptaCheck: CheckBox
    private lateinit var resultsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_finder)

        findViewById<TextView>(R.id.topBarTitle).text = "Phone Finder"

        searchBox = findViewById(R.id.phoneSearchBox)
        brandBox = findViewById(R.id.filterBrand)
        chipsetBox = findViewById(R.id.filterChipset)
        displayBox = findViewById(R.id.filterDisplay)
        refreshBox = findViewById(R.id.filterRefreshRate)
        ramBox = findViewById(R.id.filterRam)
        batteryBox = findViewById(R.id.filterBattery)
        ptaCheck = findViewById(R.id.checkPtaApproved)
        resultsContainer = findViewById(R.id.phoneResultsContainer)

        findViewById<Button>(R.id.btnFindPhones).setOnClickListener {
            renderPhones()
        }

        FirebaseRefs.products.get().addOnSuccessListener { snapshot ->
            phones.clear()

            for (child in snapshot.children) {
                val product = child.getValue(Product::class.java)
                if (product != null && product.subCategory == "mobile_phone" && product.active) {
                    phones.add(product)
                }
            }

            renderPhones()
        }
    }

    private fun renderPhones() {
        resultsContainer.removeAllViews()

        val general = searchBox.text.toString().trim().lowercase()
        val brand = brandBox.text.toString().trim().lowercase()
        val chipset = chipsetBox.text.toString().trim().lowercase()
        val display = displayBox.text.toString().trim().lowercase()
        val refresh = refreshBox.text.toString().trim().lowercase()
        val ram = ramBox.text.toString().trim().lowercase()
        val battery = batteryBox.text.toString().trim().lowercase()
        val ptaOnly = ptaCheck.isChecked

        val filtered = phones.filter { phone ->
            fun spec(key: String) = phone.specs[key]?.lowercase() ?: ""

            val matchesGeneral = general.isEmpty() ||
                    phone.title.lowercase().contains(general) ||
                    phone.brand.lowercase().contains(general) ||
                    phone.specs.entries.any {
                        it.key.lowercase().contains(general) || it.value.lowercase().contains(general)
                    }

            val matchesBrand = brand.isEmpty() || phone.brand.lowercase().contains(brand)
            val matchesChipset = chipset.isEmpty() || spec("chipset").contains(chipset)
            val matchesDisplay = display.isEmpty() || spec("displayType").contains(display)
            val matchesRefresh = refresh.isEmpty() || spec("refreshRate").contains(refresh)
            val matchesRam = ram.isEmpty() || spec("ram").contains(ram)
            val matchesBattery = battery.isEmpty() || spec("batteryMah").contains(battery)
            val matchesPta = !ptaOnly || spec("ptaApproved").contains("true") || spec("ptaApproved").contains("yes")

            matchesGeneral &&
                    matchesBrand &&
                    matchesChipset &&
                    matchesDisplay &&
                    matchesRefresh &&
                    matchesRam &&
                    matchesBattery &&
                    matchesPta
        }

        if (filtered.isEmpty()) {
            resultsContainer.addView(TextView(this).apply {
                text = "No phones found."
                textSize = 16f
                setTextColor(getColor(R.color.primary_text))
            })
            return
        }

        filtered.forEach { phone ->
            val card = TextView(this).apply {
                text = """
                    ${phone.title}
                    ${phone.brand}
                    
                    Chipset: ${phone.specs["chipset"] ?: "-"}
                    Display: ${phone.specs["displayType"] ?: "-"} ${phone.specs["refreshRate"] ?: ""}
                    RAM: ${phone.specs["ram"] ?: "-"}
                    Storage: ${phone.specs["internalStorage"] ?: "-"}
                    Battery: ${phone.specs["batteryMah"] ?: "-"}
                    PTA: ${phone.specs["ptaApproved"] ?: "-"}
                    
                    Rs ${phone.price}
                """.trimIndent()
                textSize = 15f
                setTextColor(getColor(R.color.primary_text))
                setPadding(24, 24, 24, 24)
                setBackgroundResource(R.drawable.bg_dashboard_card)
                setOnClickListener {
                    startActivity(
                        Intent(this@PhoneFinderActivity, ProductDetailActivity::class.java)
                            .putExtra("productId", phone.id)
                    )
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 14)

            resultsContainer.addView(card, params)
        }
    }
}