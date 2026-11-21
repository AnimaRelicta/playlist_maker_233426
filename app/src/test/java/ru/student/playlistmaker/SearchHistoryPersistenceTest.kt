package ru.student.playlistmaker

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.student.playlistmaker.data.SearchHistoryPreferences

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SearchHistoryPersistenceTest {
    @get:Rule val directory = TemporaryFolder()

    @Test fun storesTenUniqueQueriesInRecencyOrderAcrossReopen() = runBlocking {
        val path = directory.root.resolve("history.preferences_pb")
        val writer = Job()
        try {
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + writer)) { path }
            val history = SearchHistoryPreferences(store)
            history.addEntry("  ")
            assertEquals(emptyList<String>(), history.getEntries())
            for (index in 1..11) history.addEntry("Query $index")
            history.addEntry(" QUERY 4 ")
            history.addEntry("Earth, Wind & Fire")
        } finally {
            writer.cancelAndJoin()
        }

        val reader = Job()
        try {
            val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + reader)) { path }
            val history = SearchHistoryPreferences(store)
            assertEquals(
                listOf("Earth, Wind & Fire", "QUERY 4", "Query 11", "Query 10", "Query 9",
                    "Query 8", "Query 7", "Query 6", "Query 5", "Query 3"),
                history.getEntries(),
            )
        } finally {
            reader.cancelAndJoin()
        }
    }
}
