package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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

    private var buildCompatible = false

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

        findViewById<Button>(R.id.btnPlacePcBuildOrder).setOnClickListener {
            placePcBuildOrder()
        }

        loadProducts()
    }

    private fun loadProducts() {
        FirebaseRefs.products.get().addOnSuccessListener { snapshot ->
            products.clear()

            for (child in snapshot.children) {
                child.getValue(Product::class.java)?.let {
                    if (it.active && it.stock > 0) products.add(it)
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

    private fun selectedParts(): List<Product> {
        return listOfNotNull(
            selectedProduct(cpu),
            selectedProduct(mobo),
            selectedProduct(ram),
            selectedProduct(gpu),
            selectedProduct(psu),
            selectedProduct(casing),
            selectedProduct(storage)
        )
    }

    private fun checkCompatibility() {
        val selectedCpu = selectedProduct(cpu)
        val selectedMobo = selectedProduct(mobo)
        val selectedRam = selectedProduct(ram)
        val selectedGpu = selectedProduct(gpu)
        val selectedPsu = selectedProduct(psu)

        val issues = mutableListOf<String>()

        if (selectedCpu == null || selectedMobo == null || selectedRam == null || selectedPsu == null) {
            result.text = "Select at least CPU, motherboard, RAM, and PSU."
            buildCompatible = false
            return
        }

        if (selectedCpu.specs["socket"] != selectedMobo.specs["socket"]) {
            issues.add("CPU socket does not match motherboard socket.")
        }

        if (selectedRam.specs["ramType"] != selectedMobo.specs["ramType"]) {
            issues.add("RAM type does not match motherboard RAM type.")
        }

        val gpuRecommendedPsu = selectedGpu?.specs?.get("recommendedPsu")?.toIntOrNull() ?: 0
        val psuWattage = selectedPsu.specs["wattage"]?.toIntOrNull() ?: 0

        if (gpuRecommendedPsu > psuWattage) {
            issues.add("PSU wattage may be too low for selected GPU.")
        }

        buildCompatible = issues.isEmpty()

        result.text = if (buildCompatible) {
            "Build looks compatible.\nYou can place a custom PC build order."
        } else {
            issues.joinToString("\n")
        }
    }

    private fun placePcBuildOrder() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        checkCompatibility()

        if (!buildCompatible) {
            Toast.makeText(this, "Fix compatibility issues first", Toast.LENGTH_SHORT).show()
            return
        }

        val parts = selectedParts()

        if (parts.isEmpty()) {
            Toast.makeText(this, "No parts selected", Toast.LENGTH_SHORT).show()
            return
        }

        val orderId = FirebaseRefs.pcBuildOrders.push().key ?: return
        val total = parts.sumOf { it.price }

        val componentList = parts.map {
            mapOf(
                "productId" to it.id,
                "vendorId" to it.vendorId,
                "title" to it.title,
                "subCategory" to it.subCategory,
                "price" to it.price,
                "quantity" to 1
            )
        }

        val parentOrder = mapOf(
            "id" to orderId,
            "userId" to userId,
            "components" to componentList,
            "total" to total,
            "orderType" to "custom_pc_build",
            "assemblyStatus" to "pending_admin_review",
            "paymentMethod" to "cash_on_delivery",
            "paymentStatus" to "cod_pending",
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseRefs.pcBuildOrders.child(orderId).setValue(parentOrder)
            .addOnSuccessListener {
                createVendorComponentOrders(orderId, parts)
                deductComponentStock(parts)
                Toast.makeText(this, "Custom PC build order placed", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createVendorComponentOrders(parentOrderId: String, parts: List<Product>) {
        for (part in parts) {
            val subOrderId = FirebaseRefs.vendorComponentOrders.child(part.vendorId).push().key ?: continue

            val subOrder = mapOf(
                "id" to subOrderId,
                "parentPcBuildOrderId" to parentOrderId,
                "vendorId" to part.vendorId,
                "productId" to part.id,
                "title" to part.title,
                "subCategory" to part.subCategory,
                "price" to part.price,
                "quantity" to 1,
                "status" to "component_requested",
                "createdAt" to System.currentTimeMillis()
            )

            FirebaseRefs.vendorComponentOrders
                .child(part.vendorId)
                .child(subOrderId)
                .setValue(subOrder)
        }
    }

    private fun deductComponentStock(parts: List<Product>) {
        for (part in parts) {
            FirebaseRefs.products.child(part.id).child("stock").setValue((part.stock - 1).coerceAtLeast(0))
        }
    }
}