package mytunes.gui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import mytunes.be.Playlist;
import mytunes.be.Song;
import mytunes.bll.PlaylistManager;
import mytunes.bll.SearchQuery;
import mytunes.bll.SongManager;
import mytunes.bll.TimeFormat;
import mytunes.dal.ReadSongProperty;
import mytunes.gui.model.PlaylistModel;
import mytunes.gui.model.SongModel;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

/**
 * This class is the main controller class, it handles all the interactions
 * occuring in the main view.
 *
 * @author Stephan Fuhlendorff, Jacob Enemark, Thomas Hansen, Simon Birkedal
 */
public class MainViewController implements Initializable {

    private final SongManager songManager;
    private final PlaylistManager playlistManager;
    private final SearchQuery searchQuery;
    private final SongModel songModel;
    private final PlaylistModel playlistModel;
    private final ObservableList<Song> songsLibrary;
    private Stage primaryStage;
    private final ObservableList<Song> currentSongsInView;
    private final ObservableList<Song> searchedSongs;
    private ObservableList<Playlist> playlists;
    private Song selectedSong;
    private Playlist selectedPlaylist;
    private boolean isPlaying;
    private boolean isMuted;
    private boolean hasBrowseButtonBeenClicked;
    private double sliderVolumeValue;
    private boolean isShuffleToggled;
    private boolean isRepeatToggled;
    private final Random rand;
    private ReadSongProperty rsp;

    @FXML
    public AnchorPane mainPane;
    @FXML
    public ImageView imgMute;
    @FXML
    public ImageView imgPlay;
    @FXML
    public TextField txtSearch;
    @FXML
    public Label lblSongPlaying;
    @FXML
    public Label lblSongDuration;
    @FXML
    public Label lblTimeElapsed;
    @FXML
    public Slider sliderVolume;
    @FXML
    public ProgressBar barMediaTimer;
    @FXML
    public TableView<Song> tableSongs;
    @FXML
    public TableColumn<Song, String> colTitle;
    @FXML
    public TableColumn<Song, String> colArtist;
    @FXML
    public TableColumn<Song, String> colGenre;
    @FXML
    public TableColumn<Song, Double> colDuration;
    @FXML
    public TableColumn<Song, Double> colRating;
    @FXML
    public TableView<Playlist> tablePlaylists;
    @FXML
    public TableColumn<Playlist, String> colPlaylist;
    @FXML
    public TableColumn<Playlist, String> colTime;
    @FXML
    public Menu menuAddToPL;
    @FXML
    public Menu fileAddToPL;
    @FXML
    public ContextMenu contextSong;
    @FXML
    public MenuItem itemAddSong;
    @FXML
    public MenuItem itemEdit;
    @FXML
    public MenuItem itemDelete;
    @FXML
    public MenuBar menuBar;
    @FXML
    public Button btnPrev;
    @FXML
    public ImageView imgPrev;
    @FXML
    public Button btnPlay;
    @FXML
    public Button btnNext;
    @FXML
    public ImageView imgNext;
    @FXML
    public Hyperlink hlinkBrowse;
    @FXML
    public Label lblClearSearch;
    @FXML
    public ImageView imgShuffle;
    @FXML
    public ImageView imgRepeat;

    /**
     * The default contructor for this class.
     */
    public MainViewController()
    {
        this.isShuffleToggled = false;
        this.isRepeatToggled = false;
        this.hasBrowseButtonBeenClicked = true;
        this.rand = new Random();
        this.searchQuery = new SearchQuery();
        this.songManager = new SongManager();
        this.playlistManager = new PlaylistManager();
        this.songModel = SongModel.getInstance();
        this.playlistModel = PlaylistModel.getInstance();
        this.searchedSongs = FXCollections.observableArrayList();
        this.songsLibrary = FXCollections.observableArrayList();
        this.currentSongsInView = FXCollections.observableArrayList();
        this.playlists = FXCollections.observableArrayList();
    }

    /**
     * Loads the default settings.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {

        try
        {
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
            colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            colPlaylist.setCellValueFactory(new PropertyValueFactory<>("title"));
            colTime.setCellValueFactory(new PropertyValueFactory<>("totalDuration"));
            tableSongs.setItems(songModel.getSongs());
            currentSongsInView.setAll(songsLibrary);
            setPlaylists();
            tablePlaylists.setItems(playlists);
            initialLoad();
            isPlaying = false;
            processVolumeData();
            searchOnUpdate();
        }
        catch (Exception ex)
        {
            showErrorDialog("Initializing Error", "INITIALIZE FAIILED!", "We coulnd't load the required data.");
        }
    }

    @FXML
    private void handleAddSongButton()
    {
        addSong();
    }

    @FXML
    private void handleNextSong()
    {
        prevNextSong(true);
    }

    @FXML
    private void handlePreviousSong()
    {
        prevNextSong(false);
    }

    @FXML
    private void handleDeleteSong()
    {
        deleteSong();
        
    }

    @FXML
    private void handleAddPlaylistButton()
    {
        addPlaylist();
    }

    @FXML
    private void handleRenamePlaylist()
    {
        renamePlaylist();

    }

    /**
     * Handles the selected element whenever a mouse event occurs.
     *
     * @param event The mouse event to listen for.
     */
    @FXML
    private void handleOnMousePressed(MouseEvent event)
    {
        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();

        if (selectedSong != null)
        {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2)
            {
                songManager.playSong(selectedSong, true);

                changePlayButton(false);
                songManager.adjustVolume(sliderVolume.getValue() / 100);
                processMediaUpdates();
            }
        }
    }

    @FXML
    private void handlePlayButton()
    {
        // Making sure the song is never null before trying to play a song.
        if (selectedSong == null)
        {
            tableSongs.selectionModelProperty().get().select(0);
        }

        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();

        //Play button pressed
        if (!isPlaying)
        {
            songManager.playSong(selectedSong, false);
        }
        else
        {
            songManager.pauseSong();
        }

        changePlayButton(isPlaying);
        processMediaUpdates();
    }

    @FXML
    private void handleOnPlaylistSelected()
    {
        selectedPlaylist = tablePlaylists.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null)
        {
            currentSongsInView.clear();
            if (!selectedPlaylist.getSongList().isEmpty())
            {
                for (Song song : selectedPlaylist.getSongList())
                {
                    currentSongsInView.add(song);
                }
            }
            hasBrowseButtonBeenClicked = false;
            tableSongs.setItems(currentSongsInView);
        }
    }

    @FXML
    private void handleMuteSound()
    {
        Image image;
        if (!isMuted)
        {
            sliderVolumeValue = sliderVolume.getValue();
            image = new Image(getClass().getResourceAsStream("/mytunes/images/mute.png"));
            sliderVolume.setValue(0.0);
            isMuted = true;
        }
        else
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
            sliderVolume.setValue(sliderVolumeValue);
            isMuted = false;
        }

        imgMute.setImage(image);
    }

    @FXML
    private void handleClearSearch()
    {
        txtSearch.setText("");
    }

    @FXML
    private void handleBrowseOnAction()
    {
        tableSongs.requestFocus();
        tableSongs.setItems(songModel.getSongs());
        hasBrowseButtonBeenClicked = true;
    }

    @FXML
    private void handleDeletePlaylist()
    {
        deletePlaylist();
    }

    @FXML
    private void handleContextPLMenu()
    {
        updateAddToPlaylistMenu(menuAddToPL);
        updateAddToPlaylistMenu(fileAddToPL);
    }

    @FXML
    private void handleProgressBarDrag(MouseEvent event)
    {
        double mousePos = event.getX();
        double width = barMediaTimer.getWidth();
        double diff = 100 / width * mousePos;
        double length = songManager.getSongLength().toSeconds();
        double lenghtDiff = length / 100 * diff;

        songManager.getMediaPlayer().seek(Duration.seconds(lenghtDiff));
    }

    @FXML
    private void macros(KeyEvent key)
    {
        if (key.getCode() == KeyCode.SPACE)
        {
            handlePlayButton();
        }

        if (key.getCode() == KeyCode.DELETE)
        {
            if (key.getSource() == tablePlaylists)
            {

                deletePlaylist();
            }

            if (key.getSource() == tableSongs)
            {
                deleteSong();
            }
        }

        if (key.isControlDown())
        {
            if (selectedSong != null)
            {
                if (key.getCode() == KeyCode.UP)
                {
                    moveSong(true);
                }
                if (key.getCode() == KeyCode.DOWN)
                {
                    moveSong(false);
                }
            }

            if (key.getSource() == mainPane)
            {
                if (KeyCode.N == key.getCode())
                {
                    addSong();
                }

                if (KeyCode.P == key.getCode())
                {
                    addPlaylist();
                }
            }
        }
    }

    @FXML
    private void handleMoveSongUp()
    {
        moveSong(true);
    }

    @FXML
    private void handleMoveSongDown()
    {
        moveSong(false);
    }

    @FXML
    private void handleShuffle(ActionEvent event)
    {
        isShuffleToggled = !isShuffleToggled;
    }

    @FXML
    private void handleRepeat(ActionEvent event)
    {
        isRepeatToggled = !isRepeatToggled;
    }

    @FXML
    private void handleEditSong()
    {
        songModel.setContextSong(selectedSong);
        try
        {
            loadStage("EditSongView.fxml");
        }
        catch (IOException ex)
        {
            showErrorDialog("I/O Exception", "DATASTREAM FAILED!", "Please select a song first.");
        }
    }

    @FXML
    private void handleDragOver(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
        {
            event.acceptTransferModes(TransferMode.COPY);
        }
        else
        {
            event.consume();
        }
    }

    @FXML
    private void handleDragDropped(DragEvent event)
    {
        try
        {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles())
            {
                success = true;
                for (File file : db.getFiles())
                {
                    String filePath = file.getPath();
                    if (file.isDirectory())
                    {
                        Files.walk(Paths.get(filePath))
                                .filter(Files::isRegularFile)
                                .forEach((directoryFilePath) -> {
                                    try
                                    {
                                        if (directoryFilePath.toString().contains(".mp3"))
                                        {
                                            readDraggedSongData(directoryFilePath.toString());
                                        }
                                    }
                                    catch (Exception ex)
                                    {
                                        showErrorDialog("Unexpected File", "NOT SUPPORTED!", "Sorry, we only support MP3 Files at the moment.");
                                    }
                                });
                    }
                    else
                    {
                        readDraggedSongData(filePath);
                    }
                }
                tablePlaylists.refresh();
                tableSongs.refresh();
            }
            event.setDropCompleted(success);
            event.consume();
        }
        catch (Exception ex)
        {
            showErrorDialog("Unexpected File", "NOT SUPPORTED!", "Sorry, we only support MP3 Files at the moment.");
        }
    }    
    
    @FXML
    private void handleAbout()
    {
        String contentText = "Current functions in MyTunes are:\n"
                + "• In the file menu you can:\n"
                + "\t› Create a new song\n"
                + "\t› Create a new playlist\n"
                + "\t› Add a song to a playlist\n"
                + "• In the edit menu you can:\n"
                + "\t› Edit a song\n"
                + "\t› Move a song up or down\n"
                + "\t› Delete a song\n"
                + "\t› Rename a playlist\n"
                + "\t› Delete a playlist\n"
                + "• In the help menu you can:\n"
                + "\t› Open this dialogue\n"
                + "• In the top of the application you are able to:\n"
                + "\t› Play and pause a song\n"
                + "\t› Play the next or a previous song\n"
                + "\t› Shuffle or repeat songs\n"
                + "\t› Change volume including mute\n"
                + "\t› See and set the elapsed time of a song\n"
                + "\t› Search for a song or playlist\n"
                + "• On the left hand side you see:\n"
                + "\t› A browse button for showing your library of songs\n"
                + "\t› All playlists and their total time\n"
                + "\t› If you right-click you can add, rename and delete a playlist\n"
                + "• The main view contains the songs\n"
                + "\t› If you select a playlist it will show the songs in that playlist\n"
                + "\t› If you right-click you can add, rename and delete songs\n"
                + "\t› You can also add the selected song to a playlist\n"
                + "• The following shortcuts works in this version:\n"
                + "\t› Add song - ctrl+N\n"
                + "\t› Add playlist ctrl+P\n"
                + "\t› Delete selected - Del\n"
                + "\t› Move song up - ctrl+Up\n"
                + "\t› Move song down - ctrl+Down\n"
                + "\n"
                + "\n"
                + "\n"
                + "Made by Thomas Hansen, Jacob Enemark, Simon Birkedal and Stephan Fuhlendorff"
                + "\n";
        Alert about = new Alert(AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("About MyTunes 4.5.1");
        about.setContentText(contentText);
        about.getDialogPane().setPrefWidth(480);
        about.resizableProperty().set(true);
        about.showAndWait();
        
    }

    /**
     * Shows an error dialog.
     *
     * @param title The Window Title.
     * @param header The header title.
     * @param message The messageinformation.
     */
    private void showErrorDialog(String title, String header, String message)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Loads the add song view stage.
     */
    private void addSong()
    {
        try
        {
            loadStage("AddSongView.fxml");
        }
        catch (IOException ex)
        {
            showErrorDialog("I/O Exception", "DATASTREAM FAILED!", "The requested view could not be loaded.");
        }
    }

    /**
     * Loads the add playlist view stage.
     */
    private void addPlaylist()
    {
        try
        {
            loadStage("AddPlaylistView.fxml");
        }
        catch (IOException ex)
        {
            showErrorDialog("I/O Exception", "DATASTREAM FAILED!", "The requested view could not be loaded.");
        }
    }

    /**
     * Loads the rename playlist view stage.
     */
    private void renamePlaylist()
    {
        playlistModel.setContextPlaylist(selectedPlaylist);
        try
        {
            loadStage("RenamePlaylistView.fxml");
        }
        catch (IOException ex)
        {
            showErrorDialog("I/O Exception", "DATASTREAM FAILED!", "Please select a playlist first.");
        }
    }

    /**
     * Whenever the txtSearch field receives a new input, the songsview update
     * and sets its view to only display songs that matches the search criteria.
     */
    private void searchOnUpdate()
    {
        txtSearch.textProperty().addListener((ObservableValue<? extends String> listener, String oldQuery, String newQuery)
                -> 
                {
                    searchedSongs.setAll(searchQuery.search(getCurrentSongs(), newQuery));
                    tableSongs.setItems(searchedSongs);
        });
    }

    /**
     * Gets the current songs in the songsview.
     *
     * @return Returns an observablelist value representing the songs in the
     * currently viewed viewstage.
     */
    private ObservableList<Song> getCurrentSongs()
    {
        // If we are in the library view.
        if (selectedPlaylist == null || hasBrowseButtonBeenClicked)
        {
            currentSongsInView.setAll(songModel.getSongs());
        }
        else // If we are in a playlist's view.
        {
            currentSongsInView.setAll(selectedPlaylist.getSongList());
        }

        return currentSongsInView;
    }

    /**
     * Whenever the slidervolume value changes, the mediaplayer's volume
     * changes.
     */
    private void processVolumeData()
    {
        sliderVolume.valueProperty().addListener((ObservableValue<? extends Number> listener, Number oldVal, Number newVal)
                -> 
                {
                    if (songManager.getMediaPlayer() != null)
                    {
                        songManager.adjustVolume(newVal.doubleValue() / 100);
                        Image image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
                        imgMute.setImage(image);
                        isMuted = false;
                    }
        });
    }

    /**
     * Loads a new view on top of the main stage.
     *
     * @param viewName The view file to be loaded.
     */
    private void loadStage(String viewName) throws IOException
    {
        primaryStage = (Stage) tableSongs.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/" + viewName));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));

        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(primaryStage);

        newStage.show();
    }

    /**
     * Update playlists.
     */
    private void setPlaylists()
    {
        playlists = playlistModel.getPlaylists();
    }

    /**
     * Changes the play buttons imagery.
     *
     * @param playing Whether or not a song is playing.
     */
    private void changePlayButton(boolean playing)
    {
        Image image;
        if (playing)
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/play.png"));
            imgPlay.setImage(image);
            isPlaying = false;
        }
        else
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/pause.png"));
            imgPlay.setImage(image);
            isPlaying = true;
        }
    }

    /**
     * This methods takes care of all media player updates. When a change
     * happens in the current song's playing time, the UI elements reacts to the
     * changes.
     */
    private void processMediaUpdates()
    {
        songManager.getMediaPlayer().currentTimeProperty().addListener((ObservableValue<? extends Duration> listener, Duration oldVal, Duration newVal)
                -> 
                {
                    this.lblTimeElapsed.setText(TimeFormat.formatDouble(newVal.toSeconds()));

                    double timeElapsed = newVal.toMillis() / songManager.getSongLength().toMillis();
                    this.barMediaTimer.setProgress(timeElapsed);

                    if (songManager.getCurrentlyPlayingSong().getDuration().isEmpty() && barMediaTimer.getProgress() >= 0.999
                            || !songManager.getCurrentlyPlayingSong().getDuration().isEmpty() && barMediaTimer.getProgress() >= 1)
                    {
                        if (isRepeatToggled)
                        {
                            prevNextSong(false);
                        }
                        else
                        {
                            prevNextSong(true);
                        }
                    }
        });

        lblSongPlaying.setText(songManager.getCurrentlyPlayingSong().getTitle());
        lblSongDuration.setText(songManager.getCurrentlyPlayingSong().getDuration());
    }

    /**
     * Deletes a song, if a playlist is selected the song will only be removed
     * from that list. Otherwise all instances of the song will be removed.
     */
    private void deleteSong()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete song");
        alert.setHeaderText("Do you want to delete this song?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        {
            if (tableSongs.getItems() == currentSongsInView)
            {
                selectedPlaylist.getSongList().remove(selectedSong);
                tableSongs.getItems().remove(selectedSong);
            }
            else
            {
                tableSongs.getItems().remove(selectedSong);
                deleteFromLibrary();
            }
        }
        else
        {
            alert.close();
        }
        tablePlaylists.refresh();
        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();
    }

    /**
     * Permanently deletes a song from the library.
     */
    private void deleteFromLibrary()
    {
        ArrayList<Song> toBeDeleted = new ArrayList<>();
        for (Playlist playlist : playlists)
        {
            for (Song song : playlist.getSongList())
            {
                if (song.getId().equals(selectedSong.getId()))
                {
                    toBeDeleted.add(song);
                }
            }
            if (!toBeDeleted.isEmpty())
            {
                playlist.getSongList().removeAll(toBeDeleted);
            }
        }
    }

    /**
     * Loads all the stored data, if no datafile is found, a new datafile is
     * created.
     */
    private void initialLoad() throws Exception
    {
        try
        {
            songModel.loadSongData();
            playlistModel.loadPlaylistData();
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Generating song and playlist .dat files...");
            songModel.saveSongData();
            playlistModel.savePlaylistData();
        }
    }

    /**
     * Deletes a playlist and all of it's content.
     */
    private void deletePlaylist()
    {
        PlaylistModel.getInstance().getPlaylists().remove(selectedPlaylist);
        tablePlaylists.refresh();
    }

    /**
     * Updates the playlists in the contextmenu
     *
     * @param menu to be updated
     * @return if there is a playlist or not
     */
    private boolean updateAddToPlaylistMenu(Menu menu)
    {
        List<MenuItem> playlistSubMenu = new ArrayList<>();
        for (Playlist playlist : playlists)
        {
            MenuItem item = new MenuItem(playlist.getTitle());
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event)
                {

                    if (playlist.getSongList().contains(selectedSong))
                    {
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Song Duplicate");
                        alert.setContentText(playlist.getTitle() + " already contains a song with the title " + selectedSong.getTitle() + ". \n\nYou are about to make a copy of it.");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK)
                        {
                            playlistManager.addSong(playlist, selectedSong);
                        }
                        else
                        {
                            alert.close();
                        }
                    }
                    else
                    {
                        playlistManager.addSong(playlist, selectedSong);
                    }
                    tablePlaylists.refresh();
                }
            });
            playlistSubMenu.add(item);
        }
        if (playlistSubMenu.isEmpty())
        {
            MenuItem empty = new MenuItem("<Empty>");
            empty.setDisable(true);
            menu.getItems().set(0, empty);
            return true;
        }
        menu.getItems().setAll(playlistSubMenu);
        return false;
    }

    /**
     * Plays the song prior to the current song or the song next to the current
     * song.
     *
     * @param next If true the next song is played, otherwise the previous song
     * is played.
     */
    private void prevNextSong(boolean next)
    {
        TableViewSelectionModel<Song> selectionModel = tableSongs.selectionModelProperty().getValue();
        int selectedSongIndex = selectionModel.getSelectedIndex();
        int tableSongsTotalItems = tableSongs.getItems().size() - 1;

        if (next)
        {
            if (isShuffleToggled)
            {
                selectionModel.clearAndSelect(rand.nextInt(tableSongs.getItems().size()));
            }
            else if (selectedSongIndex == tableSongsTotalItems || selectedSong == null)
            {
                selectionModel.clearAndSelect(0);
            }
            else
            {
                selectionModel.clearAndSelect(selectedSongIndex + 1);
            }
        }
        else if (songManager.getSongTimeElapsed().toMillis() <= 3500.0)
        {
            if (selectedSongIndex == 0 || selectedSong == null)
            {
                selectionModel.clearAndSelect(tableSongsTotalItems);
            }
            else
            {
                selectionModel.clearAndSelect(selectedSongIndex - 1);
            }
        }
        else
        {
            selectionModel.clearAndSelect(selectedSongIndex);
        }
        selectedSong = selectionModel.getSelectedItem();

        songManager.playSong(selectedSong, true);
        songManager.adjustVolume(sliderVolume.getValue() / 100);

        changePlayButton(false);
        processMediaUpdates();

    }

    /**
     * Moves a song up or down.
     *
     * @param up If true the song is moved up the list, otherwise the song is
     * moved down the list.
     */
    private void moveSong(boolean up)
    {
        System.out.println(tableSongs.getSelectionModel().getSelectedIndex());
        int currIndex = tableSongs.getSelectionModel().getSelectedIndex();
        int changeIndex = currIndex;
        boolean change = false;

        if (up && currIndex != 0)
        {
            changeIndex = currIndex - 1;
            change = true;
        }
        else if (!up && currIndex != tableSongs.getItems().size() - 1)
        {
            changeIndex = currIndex + 1;
            change = true;
        }

        if (change)
        {
            Song changeSong = tableSongs.getItems().get(changeIndex);
            tableSongs.getItems().set(changeIndex, selectedSong);
            tableSongs.getItems().set(currIndex, changeSong);
            selectedSong = tableSongs.getItems().get(changeIndex);
            if (!hasBrowseButtonBeenClicked)
            {
                selectedPlaylist.getSongList().set(changeIndex, selectedSong);
                selectedPlaylist.getSongList().set(currIndex, changeSong);
            }
        }
    }
    
    /**
     * Reads the data from the dragged elements.
     * @param filePath The filepath to read the dragged data from. 
     * @throws TagException
     * @throws IOException
     * @throws InvalidAudioFrameException
     * @throws CannotReadException
     * @throws ReadOnlyFileException 
     */
    private void readDraggedSongData(String filePath) throws TagException, IOException, InvalidAudioFrameException, CannotReadException, ReadOnlyFileException
    {
        rsp = new ReadSongProperty(filePath);

        String title = rsp.getTitle();
        String artist = rsp.getArtist();
        String genre = rsp.getGenre();
        String duration = TimeFormat.formatDouble(rsp.getDuration());
        Song song = new Song(title, artist, genre, duration, 0, filePath);
        if (selectedPlaylist != null && !hasBrowseButtonBeenClicked)
        {
            playlistManager.addSong(selectedPlaylist, song);
            songModel.getSongs().add(song);
            tableSongs.getItems().add(song);
        }
        else
        {
            songModel.getSongs().add(song);
        }
    }

}
