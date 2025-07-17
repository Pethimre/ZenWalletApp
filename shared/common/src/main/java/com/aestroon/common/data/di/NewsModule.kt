package com.aestroon.common.data.di

import com.aestroon.common.data.repository.RssNewsRepository
import com.aestroon.common.domain.NewsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val newsModule = module {
    single { RssNewsRepository() }
    viewModel { NewsViewModel(get()) }
}
