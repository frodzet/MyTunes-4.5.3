package mytunes.bll;

import mytunes.be.Playlist;
import mytunes.be.Song;

/**
 * Adds and removes songs to and from a playlist.
 * @author Stephan Fuhlendorff, Jacob Enemark, Thomas Hansen, Simon Birkedal
 */
public class PlaylistManager {

    /**
     * Adds a specified song to a specified playlist.
     *
     * @param playlist A playlist to add the song to.
     * @param song The song to be added to the playlist.
     */
    public void addSong(Playlist playlist, Song song)
    {
        // TODO: Add exception handling.
        playlist.getSongList().add(song);
    }
    
}
