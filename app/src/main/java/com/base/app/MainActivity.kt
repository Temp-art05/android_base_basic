package com.base.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.di.ThemeHolder
import com.base.app.domain.model.ThemeMode
import com.base.app.navigation.AppNavGraph
import org.koin.android.ext.android.get

/**
 * Dùng AppCompatActivity (không phải ComponentActivity) vì in-app language picker gọi
 * AppCompatDelegate.setApplicationLocales() — API 33+ thì hệ thống lo, dưới 33 thì
 * AppCompat lo, và nó cần activity là AppCompat.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() đã tự gọi setDecorFitsSystemWindows(false) — không gọi lại.
        // Cũng không ép immersive/hide navigation bar: đó là quyết định của từng app,
        // không phải mặc định của base.
        enableEdgeToEdge()

        val themeHolder = get<ThemeHolder>()
        setContent {
            val theme by themeHolder.theme.collectAsStateWithLifecycle()
            AppTheme(darkTheme = theme == ThemeMode.Dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(navController = rememberNavController())
                }
            }
        }
    }
}
