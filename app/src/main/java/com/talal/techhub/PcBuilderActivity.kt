package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Product

class PcBuilderActivity : AppCompatActivity() {

    private val products = mutableListOf<Product>()

    private lateinit var cpu: Spinner
    private lateinit var mobo: Spinner
    private lateinit var ram: Spinner
    private lateinit var gpu: Spinner
    private lateinit var psu: Spinner
    private lateinit var casing: Spinner
    private lateinit var storage: Spinner
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc_builder)

        findViewById<TextView>(R.id.topBarTitle).text = "PC Builder"

        cpu = findViewById(R.id.spinnerCpu)
        mobo = findViewById(R.id.spinnerMobo)
        ram = findViewById(R.id.spinnerRam)
        gpu = findViewById(R.id.spinnerGpu)
        psu = findViewById(R.id.spinnerPsu)
        casing = findViewById(R.id.spinnerCasing)
        storage = findViewById(R.id.spinnerStorage)
        result = findViewById(R.id.txtCompatibilityResult)

        findViewById<Button>(R.id.btnCheckCompatibility).setOnClickListener {
            checkCompatibility()
        }

        loadProducts()
    }

    private fun loadProducts() {
        FirebaseRefs.products.get().addOnSuccessListener { snapshot ->
            products.clear()

            for (child in snapshot.children) {
                child.getValue(Product::class.java)?.let {
                    products.add(it)
                }
            }

            setupSpinner(cpu, "cpu")
            setupSpinner(mobo, "mobo")
            setupSpinner(ram, "ram")
            setupSpinner(gpu, "gpu")
            setupSpinner(psu, "psu")
            setupSpinner(casing, "casing")
            setupSpinner(storage, "storage")
        }
    }

    private fun setupSpinner(spinner: Spinner, type: String) {
        val items = products.filter { it.subCategory == type }

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items.map { "${it.title} - Rs ${it.price}" }
        )

        spinner.tag = items
    }

    @Suppress("UNCHECKED_CAST")
    private fun selectedProduct(spinner: Spinner): Product? {
        val items = spinner.tag as? List<Product> ?: return null
        return items.getOrNull(spinner.selectedItemPosition)
    }

    private fun checkCompatibility() {
        val selectedCpu = selectedProduct(cpu)
        val selectedMobo = selectedProduct(mobo)
        val selectedRam = selectedProduct(ram)
        val selectedGpu = selectedProduct(gpu)
        val selectedPsu = selectedProduct(psu)

        val issues = mutableListOf<String>()

        if (selectedCpu == null || selectedMobo == null || selectedRam == null) {
            result.text = "Select at least CPU, motherboard, and RAM."
            return
        }

        if (selectedCpu.specs["socket"] != selectedMobo.specs["socket"]) {
            issues.add("CPU socket does not match motherboard socket.")
        }

        if (selectedRam.specs["ramType"] != selectedMobo.specs["ramType"]) {
            issues.add("RAM type does not match motherboard RAM type.")
        }

        val gpuRecommendedPsu = selectedGpu?.specs?.get("recommendedPsu")?.toIntOrNull() ?: 0
        val psuWattage = selectedPsu?.specs?.get("wattage")?.toIntOrNull() ?: 0

        if (gpuRecommendedPsu > psuWattage) {
            issues.add("PSU wattage may be too low for selected GPU.")
        }

        result.text = if (issues.isEmpty()) {
            "Build looks compatible for current MVP checks."
        } else {
            issues.joinToString("\n")
        }
    }
}