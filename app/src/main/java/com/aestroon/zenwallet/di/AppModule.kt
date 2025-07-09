import androidx.room.Room
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.AuthRepositoryImpl
import com.aestroon.common.data.repository.UserRepository
import com.aestroon.common.data.repository.UserRepositoryImpl
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.domain.SupabaseClientProvider
import com.aestroon.authentication.domain.UserManager
import com.aestroon.calendar.domain.CalendarViewModel
import com.aestroon.common.data.database.AppDatabase
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.data.repository.CategoryRepositoryImpl
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.CurrencyConversionRepositoryImpl
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.common.utilities.network.NetworkConnectivityObserver
import com.aestroon.home.news.data.RssNewsRepository
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.common.data.repository.CurrencyRepositoryImpl
import com.aestroon.common.data.repository.PlannedPaymentRepository
import com.aestroon.common.data.repository.PlannedPaymentRepositoryImpl
import com.aestroon.common.data.repository.TransactionRepository
import com.aestroon.common.data.repository.TransactionRepositoryImpl
import com.aestroon.common.data.repository.UserPreferencesRepository
import com.aestroon.common.data.repository.UserPreferencesRepositoryImpl
import com.aestroon.common.domain.ProfileViewModel
import com.aestroon.common.data.repository.WalletRepository
import com.aestroon.common.data.repository.WalletRepositoryImpl
import com.aestroon.common.domain.TransactionsViewModel
import com.aestroon.common.domain.CategoriesViewModel
import com.aestroon.common.domain.PlannedPaymentsViewModel
import com.aestroon.common.domain.WalletsViewModel
import com.aestroon.home.news.domain.HomeViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Ktor HTTP Client for network requests
    single {
        HttpClient(Android) {
            install(HttpTimeout) {
                connectTimeoutMillis = 15000
                requestTimeoutMillis = 30000
                socketTimeoutMillis = 15000
            }
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    // Supabase client and auth
    single { SupabaseClientProvider.client.postgrest }
    single { SupabaseClientProvider.client.auth }

    // Local database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().walletDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().plannedPaymentDao() }

    // Repositories
    single<CurrencyConversionRepository> { CurrencyConversionRepositoryImpl(get(), get(), get()) }
    single<PlannedPaymentRepository> { PlannedPaymentRepositoryImpl(get(), get(), get(), get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), androidContext()) }
    single<CurrencyRepository> { CurrencyRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }
    single<WalletRepository> { WalletRepositoryImpl(get(), get(), get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get(), get()) }
    single { RssNewsRepository() }

    // Connectivity observer
    single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }

    // Session management
    single { UserManager(get()) }

    // ViewModels
    viewModel { CalendarViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { PlannedPaymentsViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { TransactionsViewModel(get(), get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { NewsViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get()) }
    viewModel { WalletsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CategoriesViewModel(get(), get()) }
}
