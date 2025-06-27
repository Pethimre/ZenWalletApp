import androidx.room.Room
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.authentication.data.AuthRepositoryImpl
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.domain.SupabaseClientProvider
import com.aestroon.authentication.domain.UserManager
import com.aestroon.common.data.database.AppDatabase
import com.aestroon.common.utilities.network.ConnectivityObserver
import com.aestroon.common.utilities.network.NetworkConnectivityObserver
import com.aestroon.home.news.data.RssNewsRepository
import com.aestroon.home.news.domain.NewsViewModel
import io.github.jan.supabase.gotrue.auth
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Supabase client and auth
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
    viewModel { NewsViewModel(get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}
