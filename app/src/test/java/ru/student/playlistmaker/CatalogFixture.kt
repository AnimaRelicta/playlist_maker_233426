package ru.student.playlistmaker

import org.koin.core.context.GlobalContext
import ru.student.playlistmaker.data.LibraryDao
import ru.student.playlistmaker.data.LocalTracksClient
import ru.student.playlistmaker.data.LocalTracksRepository
import ru.student.playlistmaker.data.TrackStorage
import ru.student.playlistmaker.domain.TracksRepository

internal fun useDemoSearch() {
    val koin = GlobalContext.get()
    koin.declare<TracksRepository>(LocalTracksRepository(LocalTracksClient(TrackStorage()), koin.get<LibraryDao>()))
}
