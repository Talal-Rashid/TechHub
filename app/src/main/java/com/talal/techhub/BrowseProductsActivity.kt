package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.data.ProductCatalog
import com.talal.techhub.models.Product

class BrowseProductsActivity : AppCompatActivity() {

    private val products = mutableListOf<Product>()

    private lateinit var container: LinearLayout
    private lateinit var searchBox: EditText
    private lateinit var filterKeySpinner: Spinner
    private lateinit var filterValueBox: EditText

    private var selectedCategory: String? = null
    private var selectedSubCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_product)

        findViewById<TextView>(R.id.topBarTitle).text = "Browse Products"

        container = findViewById(R.id.productsContainer)
        searchBox = findViewById(R.id.searchBox)
        filterKeySpinner = findViewById(R.id.filterKeySpinner)
        filterValueBox = findViewById(R.id.filterValueBox)

        findViewById<Button>(R.id.btnClearFilter).setOnClickListener {
            selectedCategory = null
            selectedSubCategory = null
            searchBox.setText("")
            filterValueBox.setText("")
            setupFilterSpinner(emptyList())
            showCategories()
        }

        searchBox.setOnEditorActionListener { _, _, _ ->
            renderProducts()
            true
        }

        filterValueBox.setOnEditorActionListener { _, _, _ ->
            renderProducts()
            true
        }

        setupFilterSpinner(emptyList())
        loadProducts()
    }

    private fun loadProducts() {
        FirebaseRefs.products.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                products.clear()

                for (child in snapshot.children) {
                    child.getValue(Product::class.java)?.let {
                        if (it.active) products.add(it)
                    }
                }

                showCategories()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BrowseProductsActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCategories() {
        container.removeAllViews()
        findViewById<TextView>(R.id.topBarTitle).text = "Product Categories"

        ProductCatalog.categories.keys.forEach { category ->
            addCard(
                title = category,
                subtitle = "${ProductCatalog.categories[category]?.size ?: 0} subcategories"
            ) {
                selectedCategory = category
                selectedSubCategory = null
                showSubCategories(category)
            }
        }
    }

    private fun showSubCategories(category: String) {
        container.removeAllViews()
        findViewById<TextView>(R.id.topBarTitle).text = category

        ProductCatalog.categories[category]?.forEach { sub ->
            val count = products.count { it.subCategory == sub }
            addCard(
                title = ProductCatalog.displayName(sub),
                subtitle = "$count products available"
            ) {
                selectedSubCategory = sub
                setupFilterSpinner(ProductCatalog.filterKeysFor(sub))
                renderProducts()
            }
        }
    }

    private fun setupFilterSpinner(keys: List<String>) {
        val items = listOf("No specific filter") + keys
        filterKeySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
    }

    private fun renderProducts() {
        container.removeAllViews()

        val sub = selectedSubCategory
        if (sub == null) {
            showCategories()
            return
        }

        findViewById<TextView>(R.id.topBarTitle).text = ProductCatalog.displayName(sub)

        val query = searchBox.text.toString().trim().lowercase()
        val filterKey = filterKeySpinner.selectedItem?.toString() ?: "No specific filter"
        val filterValue = filterValueBox.text.toString().trim().lowercase()

        val filtered = products.filter { product ->
            val matchesSubCategory = product.subCategory == sub

            val matchesSearch = query.isEmpty() ||
                    product.title.lowercase().contains(query) ||
                    product.brand.lowercase().contains(query) ||
                    product.category.lowercase().contains(query) ||
                    product.subCategory.lowercase().contains(query) ||
                    product.specs.entries.any {
                        it.key.lowercase().contains(query) || it.value.lowercase().contains(query)
                    }

            val matchesSpecificFilter =
                filterKey == "No specific filter" ||
                        filterValue.isEmpty() ||
                        product.specs[filterKey]?.lowercase()?.contains(filterValue) == true

            matchesSubCategory && matchesSearch && matchesSpecificFilter
        }

        if (filtered.isEmpty()) {
            addPlainText("No products found in this subcategory/filter.")
            return
        }

        filtered.forEach { product ->
            val stockText = if (product.stock > 0) "In stock: ${product.stock}" else "Out of stock"

            addCard(
                title = product.title,
                subtitle = "${product.brand} • Rs ${product.price}\n$stockText"
            ) {
                startActivity(
                    Intent(this, ProductDetailActivity::class.java)
                        .putExtra("productId", product.id)
                )
            }
        }
    }

    private fun addCard(title: String, subtitle: String, onClick: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(getColor(R.color.primary_text))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 14f
            setTextColor(getColor(R.color.secondary_text))
            setPadding(0, 8, 0, 0)
        }

        card.addView(titleView)
        card.addView(subtitleView)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        container.addView(card, params)
    }

    private fun addPlainText(text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(getColor(R.color.primary_text))
        })
    }
}