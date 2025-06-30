import androidx.room.Room
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.authentication.data.AuthRepositoryImpl
import com.aestroon.authentication.data.UserRepository
import com.aestroon.authentication.data.UserRepositoryImpl
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.domain.SupabaseClientProvider
import com.aestroon.authentication.domain.UserManager
import com.aestroon.common.data.database.AppDatabase
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.common.utilities.network.NetworkConnectivityObserver
import com.aestroon.home.news.data.RssNewsRepository
import com.aestroon.home.news.domain.NewsViewModel
import com.aestroon.profile.data.CurrencyRepository
import com.aestroon.profile.data.CurrencyRepositoryImpl
import com.aestroon.profile.data.UserPreferencesRepository
import com.aestroon.profile.data.UserPreferencesRepositoryImpl
import com.aestroon.profile.domain.ProfileViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
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
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    // Supabase client and auth
    single { SupabaseClientProvider.client.postgrest }
    single { SupabaseClientProvider.client }
    single { SupabaseClientProvider.client.auth }

    // Repositories
    single<AuthRepository> {
        AuthRepositoryImpl(
            auth = get(),
            userDao = get(),
            connectivityObserver = get(),
            context = androidContext()
        )
    }
    single<CurrencyRepository> { CurrencyRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }
    single { RssNewsRepository() }

    // Local database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    single { get<AppDatabase>().userDao() }

    // Connectivity observer
    single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }

    // Session management
    single { UserManager(get()) }

    // ViewModels
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { NewsViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}
