package ru.student.playlistmaker

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.student.playlistmaker.data.tracksDataModule
import ru.student.playlistmaker.ui.search.SearchViewModel

val searchModule = module { viewModel { SearchViewModel(get()) } }

class PlaylistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PlaylistApplication)
            modules(tracksDataModule, searchModule)
        }
    }
}
