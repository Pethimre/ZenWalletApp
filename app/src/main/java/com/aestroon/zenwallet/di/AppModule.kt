import com.aestroon.authentication.data.AuthRepository
import com.aestroon.authentication.data.AuthRepositoryImpl
import com.aestroon.authentication.domain.AuthViewModel
import com.aestroon.authentication.domain.SupabaseClientProvider
import com.aestroon.authentication.domain.UserManager
import com.aestroon.home.news.data.RssNewsRepository
import com.aestroon.home.news.domain.NewsViewModel
import io.github.jan.supabase.gotrue.auth
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SupabaseClientProvider.client }

    single { SupabaseClientProvider.client.auth }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { UserManager(get()) }
    single { RssNewsRepository() }

    viewModel { NewsViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
