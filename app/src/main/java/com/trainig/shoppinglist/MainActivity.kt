package com.trainig.shoppinglist

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.trainig.shoppinglist.data.ThemePreferences
import com.trainig.shoppinglist.presentation.ShoppingListScreen
import com.trainig.shoppinglist.ui.theme.ShoppingListTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShoppingListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShoppingListScreen()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply saved language preference on startup
        val context = runBlocking {
            val prefs = com.trainig.shoppinglist.data.ThemePreferencesImpl(newBase)
            val languageCode = prefs.language.first()

            // Default to English if no language is saved
            val locale = Locale(languageCode ?: "en")
            Locale.setDefault(locale)

            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)

            newBase.createConfigurationContext(config)
        }

        super.attachBaseContext(context)
    }

    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate the activity to apply the new locale
        recreate()
    }
}
