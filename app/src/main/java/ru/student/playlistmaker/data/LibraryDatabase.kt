package ru.student.playlistmaker.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import ru.student.playlistmaker.domain.Track

@Entity(tableName = "tracks")
internal data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val durationMillis: Long,
    val artwork: Track.Artwork,
    val artworkUrl: String?,
    val durationKnown: Boolean,
) {
    fun toDomain() = Track(id, title, artist, durationMillis, artwork, artworkUrl, durationKnown)
}

internal fun Track.toEntity() =
    TrackEntity(id, title, artist, durationMillis, artwork, artworkUrl, durationKnown)

@Entity(tableName = "playlists")
internal data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val coverImageUri: String? = null,
)

@Entity(
    tableName = "playlist_tracks",
    indices = [Index(value = ["playlistId", "trackId"], unique = true), Index("trackId")],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE),
    ],
)
internal data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE),
    ],
)
internal data class FavoriteEntity(@PrimaryKey val trackId: Long)

internal data class PlaylistRow(
    @Embedded val playlist: PlaylistEntity,
    @Embedded(prefix = "track_") val track: TrackEntity?,
)

private const val PLAYLIST_ROWS = """
    SELECT playlists.*,
           tracks.id AS track_id, tracks.title AS track_title, tracks.artist AS track_artist,
           tracks.durationMillis AS track_durationMillis, tracks.artwork AS track_artwork,
           tracks.artworkUrl AS track_artworkUrl, tracks.durationKnown AS track_durationKnown
    FROM playlists
    LEFT JOIN playlist_tracks ON playlists.id = playlist_tracks.playlistId
    LEFT JOIN tracks ON playlist_tracks.trackId = tracks.id
"""

@Dao
internal interface LibraryDao {
    @Upsert
    suspend fun saveTracks(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun findTrack(id: Long): TrackEntity?

    @Query(PLAYLIST_ROWS + " ORDER BY playlists.id, playlist_tracks.id")
    fun playlists(): Flow<List<PlaylistRow>>

    @Query(PLAYLIST_ROWS + " WHERE playlists.id = :id ORDER BY playlist_tracks.id")
    fun playlist(id: Long): Flow<List<PlaylistRow>>

    @Insert
    suspend fun create(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("""
        INSERT OR IGNORE INTO playlist_tracks (playlistId, trackId)
        SELECT :playlistId, :trackId
        WHERE EXISTS (SELECT 1 FROM playlists WHERE id = :playlistId)
          AND EXISTS (SELECT 1 FROM tracks WHERE id = :trackId)
    """)
    suspend fun addTrack(playlistId: Long, trackId: Long): Long

    @Query("SELECT EXISTS(SELECT 1 FROM playlists WHERE id = :id)")
    suspend fun playlistExists(id: Long): Boolean

    @Query("""
        INSERT OR IGNORE INTO playlist_tracks (playlistId, trackId)
        SELECT :targetId, trackId FROM playlist_tracks
        WHERE playlistId = :sourceId ORDER BY id
    """)
    suspend fun copyPlaylistTracks(sourceId: Long, targetId: Long)

    @Transaction
    suspend fun merge(sourceId: Long, targetId: Long): Boolean {
        if (sourceId == targetId || !playlistExists(sourceId) || !playlistExists(targetId)) return false
        copyPlaylistTracks(sourceId, targetId)
        deletePlaylist(sourceId)
        return true
    }

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrack(playlistId: Long, trackId: Long)

    @Query("SELECT tracks.* FROM tracks JOIN favorites ON favorites.trackId = tracks.id ORDER BY favorites.rowid")
    fun favorites(): Flow<List<TrackEntity>>

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: Long): Int

    @Query("INSERT INTO favorites (trackId) SELECT id FROM tracks WHERE id = :trackId")
    suspend fun addFavorite(trackId: Long)

    @Transaction
    suspend fun toggleFavorite(trackId: Long) {
        if (removeFavorite(trackId) == 0) addFavorite(trackId)
    }
}

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class LibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        fun open(context: Context): LibraryDatabase =
            Room.databaseBuilder(context, LibraryDatabase::class.java, "library.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("INSERT INTO playlists (name, description) VALUES (?, ?)", arrayOf("Best songs 2021", "My favorite tracks"))
                        db.execSQL("INSERT INTO playlists (name, description) VALUES (?, ?)", arrayOf("Summer Party", "Beach vibes"))
                    }
                })
                .build()
    }
}
