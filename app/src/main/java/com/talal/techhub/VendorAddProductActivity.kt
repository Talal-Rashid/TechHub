package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.data.ProductCatalog
import com.talal.techhub.models.Product

class VendorAddProductActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var subCategorySpinner: Spinner
    private lateinit var specsBox: EditText

    private lateinit var titleField: EditText
    private lateinit var brandField: EditText
    private lateinit var priceField: EditText
    private lateinit var stockField: EditText
    private lateinit var descriptionField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_add_product)

        findViewById<TextView>(R.id.topBarTitle).text = "Add Product"

        titleField = findViewById(R.id.productTitleEditText)
        brandField = findViewById(R.id.productBrandEditText)
        priceField = findViewById(R.id.productPriceEditText)
        stockField = findViewById(R.id.productStockEditText)
        descriptionField = findViewById(R.id.productDescriptionEditText)


        categorySpinner = findViewById(R.id.categorySpinner)
        subCategorySpinner = findViewById(R.id.subCategorySpinner)
        specsBox = findViewById(R.id.specsEditText)

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            ProductCatalog.categories.keys.toList()
        )

        categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val category = categorySpinner.selectedItem.toString()
                val subCategories = ProductCatalog.categories[category] ?: emptyList()

                subCategorySpinner.adapter = ArrayAdapter(
                    this@VendorAddProductActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    subCategories
                )
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        subCategorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val type = subCategorySpinner.selectedItem.toString()

                specsBox.setText(
                    ProductCatalog.specsFor(type).joinToString("\n") { "$it=" }
                )
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnSaveProduct).setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val vendorId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val productId = FirebaseRefs.products.push().key ?: return

        val specs = specsBox.text.toString()
            .lines()
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
            .toMap()

        val product = Product(
            id = productId,
            vendorId = vendorId,
            title = titleField.text.toString().trim(),
            brand = brandField.text.toString().trim(),
            category = categorySpinner.selectedItem.toString(),
            subCategory = subCategorySpinner.selectedItem.toString(),
            price = priceField.text.toString().toDoubleOrNull() ?: 0.0,
            stock = stockField.text.toString().toIntOrNull() ?: 0,
            description = descriptionField.text.toString().trim(),
            specs = specs,
            active = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        FirebaseRefs.products.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}