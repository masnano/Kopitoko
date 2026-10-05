package com.android.kopitoko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.kopitoko.data.ClipboardObserver
import com.android.kopitoko.data.local.ApiKeyDataStore
import com.android.kopitoko.data.repository.ApiKeyRepository
import com.android.kopitoko.data.repository.GeminiRepository
import com.android.kopitoko.ui.navigation.KopitokoApp
import com.android.kopitoko.ui.theme.KopitokoTheme
import com.android.kopitoko.ui.viewmodel.ApiKeyViewModel
import com.android.kopitoko.ui.viewmodel.CaptionViewModel
import com.android.kopitoko.ui.viewmodel.InvoiceViewModel
import com.android.kopitoko.ui.viewmodel.MainViewModel
import com.android.kopitoko.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKeyDataStore = ApiKeyDataStore(applicationContext)
        val apiKeyRepository = ApiKeyRepository(apiKeyDataStore)
        val geminiRepository = GeminiRepository(apiKeyRepository)
        val clipboardObserver = ClipboardObserver(applicationContext)

        val mainViewModel: MainViewModel by viewModels {
            ViewModelFactory { MainViewModel(apiKeyRepository) }
        }
        val apiKeyViewModel: ApiKeyViewModel by viewModels {
            ViewModelFactory { ApiKeyViewModel(apiKeyRepository, geminiRepository) }
        }
        val captionViewModel: CaptionViewModel by viewModels {
            ViewModelFactory { CaptionViewModel(geminiRepository) }
        }
        val invoiceViewModel: InvoiceViewModel by viewModels {
            ViewModelFactory { InvoiceViewModel(geminiRepository, clipboardObserver) }
        }
        val productViewModel: ProductViewModel by viewModels {
            ViewModelFactory { ProductViewModel(geminiRepository) }
        }

        setContent {
            KopitokoTheme {
                KopitokoApp(
                    mainViewModel = mainViewModel,
                    apiKeyViewModel = apiKeyViewModel,
                    captionViewModel = captionViewModel,
                    invoiceViewModel = invoiceViewModel,
                    productViewModel = productViewModel,
                )
            }
        }
    }
}

class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return creator() as VM
    }
}
