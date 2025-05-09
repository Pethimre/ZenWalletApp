import com.aestroon.zenwallet.AuthViewModel
import com.aestroon.zenwallet.LoginViewModel
import com.aestroon.zenwallet.SupabaseClientProvider
import com.aestroon.zenwallet.UserManager
import com.aestroon.zenwallet.data.AuthRepository
import com.aestroon.zenwallet.data.AuthRepositoryImpl
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

    viewModel { LoginViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
