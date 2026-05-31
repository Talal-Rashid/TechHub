package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Review

class AddReviewActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var commentEditText: EditText

    private lateinit var productId: String
    private lateinit var orderId: String
    private lateinit var productTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        findViewById<TextView>(R.id.topBarTitle).text = "Review"

        productId = intent.getStringExtra("productId") ?: return finish()
        orderId = intent.getStringExtra("orderId") ?: return finish()
        productTitle = intent.getStringExtra("productTitle") ?: "Product"

        findViewById<TextView>(R.id.txtProductName).text = productTitle

        ratingBar = findViewById(R.id.ratingBar)
        commentEditText = findViewById(R.id.commentEditText)

        findViewById<Button>(R.id.btnSubmitReview).setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val rating = ratingBar.rating.toInt()
        val comment = commentEditText.text.toString().trim()

        if (rating <= 0) {
            Toast.makeText(this, "Select rating", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewId = FirebaseRefs.reviews.child(productId).push().key ?: return

        val review = Review(
            id = reviewId,
            productId = productId,
            userId = uid,
            orderId = orderId,
            rating = rating,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )

        FirebaseRefs.reviews.child(productId).child(reviewId).setValue(review)
            .addOnSuccessListener {
                Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}