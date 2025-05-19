package com.aestroon.home.news.di

import com.aestroon.home.news.data.RssNewsRepository
import com.aestroon.home.news.domain.NewsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val newsModule = module {
    single { RssNewsRepository() }
    viewModel { NewsViewModel(get()) }
}
