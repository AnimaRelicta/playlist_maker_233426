package ru.student.playlistmaker

import org.koin.core.context.GlobalContext
import ru.student.playlistmaker.data.DatabaseMock
import ru.student.playlistmaker.data.LocalTracksClient
import ru.student.playlistmaker.data.LocalTracksRepository
import ru.student.playlistmaker.data.TrackStorage
import ru.student.playlistmaker.domain.TracksRepository

internal fun useDemoSearch() {
    val koin = GlobalContext.get()
    koin.declare<TracksRepository>(LocalTracksRepository(LocalTracksClient(koin.get<TrackStorage>()), koin.get<DatabaseMock>()))
}
