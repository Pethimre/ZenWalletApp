import com.aestroon.authentication.domain.SupabaseClientProvider
import com.aestroon.authentication.domain.UserManager
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.authentication.data.AuthRepositoryImpl
import com.aestroon.authentication.domain.AuthViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module {
    single { SupabaseClientProvider.client }
    single { get<SupabaseClient>().pluginManager.getPlugin(Auth) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { UserManager(get()) }
    single { get<SupabaseClient>().pluginManager.getPlugin(Auth) }

    viewModel { AuthViewModel(get(), get()) }
}
