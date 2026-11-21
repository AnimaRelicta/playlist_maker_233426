package ru.student.playlistmaker

import android.app.Application
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.student.playlistmaker.data.tracksDataModule
import ru.student.playlistmaker.ui.search.SearchViewModel
import ru.student.playlistmaker.ui.playlists.LibraryViewModel

val searchModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }
}

class PlaylistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PlaylistApplication)
            modules(tracksDataModule, searchModule)
        }
    }
}
