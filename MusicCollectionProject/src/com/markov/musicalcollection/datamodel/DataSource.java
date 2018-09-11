package com.markov.musicalcollection.datamodel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private static final String CONNECTION_STRING = "jdbc:sqlite:";

    // Constants for artist table and columns -->
    private static final String TABLE_ARTISTS = "artists";
    private static final String COLUMN_ARTISTS_NAME = "name";
    private static final String COLUMN_ARTISTS_ID = "_id";

    // Constants for albums table and columns -->
    private static final String TABLE_ALBUMS = "albums";
    private static final String COLUMN_ALBUMS_NAME = "name";
    private static final String COLUMN_ALBUMS_ID = "_id";
    private static final String COLUMN_ALBUMS_ARTIST = "artist";

    // Constants for songs table and columns -->
    private static final String TABLE_SONGS = "songs";
    private static final String COLUMN_SONGS_ID = "_id";
    private static final String COLUMN_SONGS_TRACK = "track";
    private static final String COLUMN_SONGS_TITLE = "title";
    private static final String COLUMN_SONGS_ALBUM = "album";

    // Constants for querying artists
    private static final String QUERY_ARTISTS = "SELECT " + COLUMN_ARTISTS_ID + ", " + COLUMN_ARTISTS_NAME + " FROM " + TABLE_ARTISTS
            + " ORDER BY " + TABLE_ARTISTS + "." + COLUMN_ARTISTS_NAME + " COLLATE NOCASE ASC";

    private static final String QUERY_ARTISTS_BY_NAME = "SELECT * FROM " + TABLE_ARTISTS + " WHERE " +
            COLUMN_ARTISTS_NAME + " LIKE ? ORDER BY " + COLUMN_ARTISTS_NAME + " COLLATE NOCASE ASC";

    private static final String QUERY_ARTIST_BY_ID = "SELECT " + COLUMN_ARTISTS_NAME + " FROM " + TABLE_ARTISTS +
            " WHERE " + COLUMN_ARTISTS_ID + " = ";

    // Constants to add/update/delete artists
    private static final String ADD_ARTIST = "INSERT INTO " + TABLE_ARTISTS + "(" + COLUMN_ARTISTS_NAME + ") VALUES (?)";

    private static final String UPDATE_ARTIST_NAME = "UPDATE " + TABLE_ARTISTS + " SET " + COLUMN_ARTISTS_NAME +
            " = ? WHERE " + COLUMN_ARTISTS_ID + " = ?";
    private static final String DELETE_ARTIST = "DELETE FROM " + TABLE_ARTISTS + " WHERE " + COLUMN_ARTISTS_ID + " = ";


    // Constants for querying albums
    private static final String QUERY_ALBUMS_BY_ARTIST_ID = "SELECT " + COLUMN_ALBUMS_ID + ", " + COLUMN_ALBUMS_NAME + ", " +
            COLUMN_ALBUMS_ARTIST + " FROM " + TABLE_ALBUMS + " WHERE " + COLUMN_ALBUMS_ARTIST + " = ";

    private static final String QUERY_ALBUMS_BY_NAME = "SELECT " + COLUMN_ALBUMS_ID + ", " + COLUMN_ALBUMS_NAME + ", " +
            COLUMN_ALBUMS_ARTIST + " FROM " + TABLE_ALBUMS + " WHERE " + COLUMN_ALBUMS_ARTIST + " = ? AND " +
            COLUMN_ALBUMS_NAME + " LIKE ? ORDER BY " + COLUMN_ALBUMS_NAME + " COLLATE NOCASE ASC";

    private static final String QUERY_ALBUM_BY_ID = "SELECT " + COLUMN_ALBUMS_NAME + " FROM " + TABLE_ALBUMS +
            " WHERE " + COLUMN_ALBUMS_ID + " = ";

    // Constants to add/update/delete albums
    private static final String ADD_ALBUM = "INSERT INTO " + TABLE_ALBUMS + "(" + COLUMN_ALBUMS_NAME +
            ", " + COLUMN_ALBUMS_ARTIST + ") VALUES (?, ?)";
    private static final String UPDATE_ALBUM_NAME = "UPDATE " + TABLE_ALBUMS + " SET " + COLUMN_ALBUMS_NAME +
            " = ? WHERE " + COLUMN_ALBUMS_ID + " = ?";
    private static final String DELETE_ALBUM = "DELETE FROM " + TABLE_ALBUMS + " WHERE " + COLUMN_ALBUMS_ID + " = ";
    private static final String DELETE_ALL_ALBUMS_BY_ARTIST = "DELETE FROM " + TABLE_ALBUMS + " WHERE " +
            COLUMN_ALBUMS_ARTIST + " = ";


    // Constants for querying songs
    private static final String QUERY_SONGS = "SELECT " + COLUMN_SONGS_ID + ", " + COLUMN_SONGS_TRACK +
            ", " + COLUMN_SONGS_TITLE + ", " + COLUMN_SONGS_ALBUM + " FROM " + TABLE_SONGS + " WHERE " + COLUMN_SONGS_ALBUM +
            " = ";
    private static final String QUERY_SONGS_BY_NAME = "SELECT " + COLUMN_SONGS_ID + ", " + COLUMN_SONGS_TRACK +
            ", " + COLUMN_SONGS_TITLE + ", " + COLUMN_SONGS_ALBUM + " FROM " + TABLE_SONGS + " WHERE " +
            COLUMN_SONGS_ALBUM + " = ? AND " + COLUMN_SONGS_TITLE + " LIKE ? " + " ORDER BY " + COLUMN_SONGS_TRACK;
    private static final String QUERY_SONGS_BY_TRACK = "SELECT * FROM " + TABLE_SONGS + " WHERE " +
            COLUMN_SONGS_TRACK + " = ? AND " + COLUMN_SONGS_ALBUM + " = ?";
    // Constants to add/update/delete songs
    private static final String ADD_SONG = "INSERT INTO " + TABLE_SONGS + "(" + COLUMN_SONGS_TRACK +
            ", " + COLUMN_SONGS_TITLE + ", " + COLUMN_SONGS_ALBUM + ") VALUES (?, ?, ?)";
    private static final String UPDATE_SONG_TITLE = "UPDATE " + TABLE_SONGS + " SET " + COLUMN_SONGS_TITLE +
            " = ? WHERE " + COLUMN_SONGS_ID + " = ?";

    private static final String DELETE_SONG = "DELETE FROM " + TABLE_SONGS + " WHERE " + COLUMN_SONGS_ID +
            " = ";
    private static final String DELETE_ALL_SONGS_IN_ALBUM = "DELETE FROM " + TABLE_SONGS + " WHERE " +
            COLUMN_SONGS_ALBUM + " = ";


    private static DataSource instance = new DataSource();
    private Connection connection;
    private PreparedStatement searchArtistsByName;
    private PreparedStatement searchAlbumsByName;
    private PreparedStatement searchSongsByTrack;
    private PreparedStatement searchSongsByName;
    private PreparedStatement updateArtistName;
    private PreparedStatement updateAlbumName;
    private PreparedStatement updateSongTitle;
    private PreparedStatement addArtist;
    private PreparedStatement addAlbum;
    private PreparedStatement addSong;

    private int currArtistId;
    private int currAlbumId;

    private DataSource() {
    }

    public static DataSource getInstance() {
        return instance;
    }

    // Connect to the database and create the prepared statements
    public boolean open(String filePath) {
        try {

            connection = DriverManager.getConnection(CONNECTION_STRING + filePath);
            searchArtistsByName = connection.prepareStatement(QUERY_ARTISTS_BY_NAME);
            searchAlbumsByName = connection.prepareStatement(QUERY_ALBUMS_BY_NAME);
            searchSongsByTrack = connection.prepareStatement(QUERY_SONGS_BY_TRACK);
            searchSongsByName = connection.prepareStatement(QUERY_SONGS_BY_NAME);
            updateArtistName = connection.prepareStatement(UPDATE_ARTIST_NAME);
            updateAlbumName = connection.prepareStatement(UPDATE_ALBUM_NAME);
            updateSongTitle = connection.prepareStatement(UPDATE_SONG_TITLE);
            addArtist = connection.prepareStatement(ADD_ARTIST);
            addAlbum = connection.prepareStatement(ADD_ALBUM);
            addSong = connection.prepareStatement(ADD_SONG);
            return true;
        } catch (SQLException e) {
            System.out.println("Error connecting with the database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if(searchArtistsByName != null) {
                searchArtistsByName.close();
            }
            if(searchAlbumsByName != null) {
                searchAlbumsByName.close();
            }
            if(searchSongsByTrack != null) {
                searchSongsByTrack.close();
            }
            if(searchSongsByName != null) {
                searchSongsByName.close();
            }
            if(updateArtistName != null) {
                updateArtistName.close();
            }
            if(updateAlbumName != null) {
                updateAlbumName.close();
            }
            if(updateSongTitle != null) {
                updateSongTitle.close();
            }
            if(addArtist != null) {
                addArtist.close();
            }
            if(addAlbum != null) {
                addAlbum.close();
            }
            if(addSong != null) {
                addSong.close();
            }
            if(connection != null) {
                connection.close();
            }
        } catch(SQLException e) {
            System.out.println("Error closing the database: " + e.getMessage());
        }
    }

    // Returns the name of the current artist (used for the infoLabel in Albums window)
    public String getCurrArtist(int id) {
        String sql = QUERY_ARTIST_BY_ID + id;

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {

            return results.getString(1);
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    // Returns the name of the current album (used for infoLabel in Songs window)
    public String getCurrAlbum(int id) {
        String sql = QUERY_ALBUM_BY_ID + id;

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {

            return results.getString(1);
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    // Returns a list of artists from the database sorted alphabetically
    public List<Artist> queryArtists() {

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(QUERY_ARTISTS)) {

            List<Artist> artists = new ArrayList<>();
            populateArtistsList(results, artists);

            return artists;
        } catch (SQLException e) {
            System.out.println("Querying artists failed: " + e.getMessage());
            return null;
        }
    }

    public List<Artist> queryArtistsByName(String name) {
        name = "%" + name + "%";

        try {
            searchArtistsByName.setString(1, name);
            ResultSet results = searchArtistsByName.executeQuery();

            List<Artist> artists = new ArrayList<>();

            populateArtistsList(results, artists);
            results.close();

            return artists;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    // Returns true if the update succeeded and the affected row is just 1
    public boolean updateArtistName(int id, String name) {
        try {
            updateArtistName.setString(1, name);
            updateArtistName.setInt(2, id);

            int affectedRows = updateArtistName.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Changing artist's name failed: " + e.getMessage());
            return false;
        }
    }

    // Checks if the artist is present in the records and if it's not adds it
    // Returns the new _id generated for this artist on success and -1 otherwise
    public int addArtist(String name) {

        try {
            connection.setAutoCommit(false);
            searchArtistsByName.setString(1, name);
            ResultSet results = searchArtistsByName.executeQuery();
            if(results.next()) {
                return -1;
            }
            addArtist.setString(1, name);

            int affectedRows = addArtist.executeUpdate();
            if(affectedRows != 1) {
                return -1;
            }

            ResultSet generatedKey = addArtist.getGeneratedKeys();
            if(generatedKey.next()) {
                connection.commit();
                return generatedKey.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            System.out.println("Adding artist failed: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Deletes all the albums and songs from the artist
    public boolean deleteArtist(int id) {

        try (Statement statement = connection.createStatement();
             Statement deleteAlbumsStatement = connection.createStatement();
             Statement deleteSongsStatement = connection.createStatement()) {

            connection.setAutoCommit(false);
            String sql = QUERY_ALBUMS_BY_ARTIST_ID + id;

            ResultSet results = statement.executeQuery(sql);
            while(results.next()) {
                String deleteSongsSql = DELETE_ALL_SONGS_IN_ALBUM + results.getInt(1);
                deleteSongsStatement.executeUpdate(deleteSongsSql);
            }
            sql = DELETE_ALL_ALBUMS_BY_ARTIST + id;
            deleteAlbumsStatement.executeUpdate(sql);

            sql = DELETE_ARTIST + id;
            statement.executeUpdate(sql);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Deleting failed: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                // Some bad things happened
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit: " + e.getMessage());
            }
        }
    }

    // Returns a list of albums for the given artist id
    public List<Album> queryAlbums(int artistId) {
        currArtistId = artistId;

        String sql = QUERY_ALBUMS_BY_ARTIST_ID + artistId + " ORDER BY " + COLUMN_ALBUMS_NAME + " COLLATE NOCASE";

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {

            List<Album> albums = new ArrayList<>();
            populateAlbumsList(results, albums);
            return albums;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Album> queryAlbumsByName(String name) {
        name = "%" + name + "%";

        try {
            searchAlbumsByName.setInt(1, currArtistId);
            searchAlbumsByName.setString(2, name);
            ResultSet results = searchAlbumsByName.executeQuery();

            List<Album> albums = new ArrayList<>();
            populateAlbumsList(results, albums);

            results.close();
            return albums;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return null;
    }

    // Checks if the artist has this album and if not adds it
    // Returns the new _id generated for this album on success and -1 otherwise
    public int addAlbum(String name) {

        try {
            searchAlbumsByName.setInt(1, currArtistId);
            searchAlbumsByName.setString(2, name);
            ResultSet results = searchAlbumsByName.executeQuery();

            if(results.next()) {
                return -1;
            }

            addAlbum.setString(1, name);
            addAlbum.setInt(2, currArtistId);
            int affectedRows = addAlbum.executeUpdate();

            if(affectedRows == 1) {
                ResultSet generatedKey = addAlbum.getGeneratedKeys();
                if(generatedKey.next()) {
                    return generatedKey.getInt(1);
                }
            }

            return -1;
        } catch (SQLException e) {
            System.out.println("Adding album failed: " + e.getMessage());
            return -1;
        }
    }

    // Returns true if the update succeeded and the affected row is just 1
    public boolean updateAlbumName(int id, String name) {

        try {
            updateAlbumName.setString(1, name);
            updateAlbumName.setInt(2, id);

            int affectedRows = updateAlbumName.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Changing album's name failed: " + e.getMessage());
            return false;
        }
    }

    // Deletes all the songs in the album and the album itself
    public boolean deleteAlbum(int id) {

        try (Statement statement = connection.createStatement()) {

            connection.setAutoCommit(false);
            String sql = DELETE_ALL_SONGS_IN_ALBUM + id;
            statement.executeUpdate(sql);

            sql = DELETE_ALBUM + id;
            System.out.println(sql);
            statement.executeUpdate(sql);

            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Deletion failed: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                // Well...
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit: " + e.getMessage());
            }
        }
    }

    // Returns a list of songs in the album
    public List<Song> querySongs(int albumId) {
        currAlbumId = albumId;

        String sql = QUERY_SONGS + albumId + " ORDER BY " + COLUMN_SONGS_TRACK;

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {

            List<Song> songs = new ArrayList<>();
            populateSongsList(results, songs);

            return songs;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Song> querySongsByName(String title, int albumId) {
        title = "%" + title + "%";

        try {
            searchSongsByName.setInt(1, albumId);
            searchSongsByName.setString(2, title);

            ResultSet results = searchSongsByName.executeQuery();
            List<Song> songs = new ArrayList<>();
            populateSongsList(results, songs);

            results.close();
            return songs;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    // Checks if the song is present or there is another song with this track No
    // in the current album and if not adds it
    // Returns the new _id generated for this song on success and -1 otherwise
    public int addSong(int track, String title) {

        try {
            searchSongsByTrack.setInt(1, track);
            searchSongsByTrack.setInt(2, currAlbumId);

            ResultSet results = searchSongsByTrack.executeQuery();
            if(results.next()) {
                return -1;
            }

            searchSongsByName.setInt(1, currAlbumId);
            searchSongsByName.setString(2, title);

            results = searchSongsByName.executeQuery();
            if(results.next()) {
                return -1;
            }

            addSong.setInt(1, track);
            addSong.setString(2, title);
            addSong.setInt(3, currAlbumId);
            int affectedRows = addSong.executeUpdate();

            if(affectedRows == 1) {
                ResultSet generatedKey = addSong.getGeneratedKeys();
                if(generatedKey.next()) {
                    return generatedKey.getInt(1);
                }
            }

            return -1;
        } catch (SQLException e) {
            System.out.println("Adding song failed: " + e.getMessage());
            return -1;
        }
    }

    // Returns true if the update succeeded and the affected row is just 1
    public boolean updateSongTitle(int id, String title) {

        try {
            updateSongTitle.setString(1, title);
            updateSongTitle.setInt(2, id);

            int affectedRows = updateSongTitle.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Changing song's title failed: " + e.getMessage());
            return false;
        }
    }

    // Deletes a song by given ID
    public boolean deleteSong(int id) {
        String sql = DELETE_SONG + id;

        try (Statement statement = connection.createStatement()) {
            int affectedRows = statement.executeUpdate(sql);
            System.out.println("Affected Rows - " + affectedRows);
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Deletion failed: " + e.getMessage());
            return false;
        }
    }

    // Creates artist for every record found and adds it to the list
    private void populateArtistsList(ResultSet results, List<Artist> artistsList) throws SQLException {
        while(results.next()) {
            Artist artist = new Artist();
            artist.setId(results.getInt(1));
            artist.setName(results.getString(2));
            artistsList.add(artist);
        }
    }

    // Creates album for every record found and adds it to the list
    private void populateAlbumsList(ResultSet results, List<Album> albumsList) throws SQLException {
        while(results.next()) {
            Album album = new Album();
            album.setId(results.getInt(1));
            album.setName(results.getString(2));
            album.setArtistId(results.getInt(3));
            albumsList.add(album);
        }
    }

    // Creates song for every record found and adds it to the list
    private void populateSongsList(ResultSet results, List<Song> songsList) throws SQLException {
        while(results.next()) {
            Song song = new Song();
            song.setId(results.getInt(1));
            song.setTrack(results.getInt(2));
            song.setTitle(results.getString(3));
            song.setAlbumId(results.getInt(4));
            songsList.add(song);
        }
    }
}
