package com.talal.techhub

import android.app.Application  // ✅ ADD THIS LINE
import com.google.firebase.database.FirebaseDatabase

class TechHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }
}
