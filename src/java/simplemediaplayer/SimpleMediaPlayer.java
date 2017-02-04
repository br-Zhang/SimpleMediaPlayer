package simplemediaplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Logger;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A Simple Media Player that plays audio files from a directory or PlayList and
 * allows saving PlayLists into a text file.
 * 
 * TODO: Suggestions (Probably never) TODO: Play lists that auto update with
 * file changes (Probably very hard)
 * 
 * @author Brendan Zhang
 * @created 2016-04-24
 * @since 2016-05-04
 */
public class SimpleMediaPlayer extends Application {

  // TODO At what point do we make separate classes for each of these...
  // Default path of the audio files to load
  String defaultPath = "C:\\Users\\Public\\Music";
  PlayList playList = new PlayList();
  // Logger to log information messages to
  private Logger LOGGER = Logger.getGlobal();
  // Label to display title of currently playing audio file
  private Label currentlyPlaying = new Label();
  // ProgressBar to show progress of song
  private ProgressBar progress = new ProgressBar();
  // Path to icon of the application
  private final static String ICON_SOURCE_PATH =
      "images/windows_media_player.png";
  // MediaView to show the song title of the currently playing MediaPlayer
  private MediaView mediaView;
  // "Next" button
  private Button next = new Button("Next");
  // Button that will be used for "Play" and "Pause
  private Button playNPause = new Button("Pause");
  private final Label slashPlaceHolder = new Label("/");
  private Label currentTime = new Label();
  private Label totalTime = new Label();
  DecimalFormat twoDigitFormat = new DecimalFormat("00");
  Tile gaugeTile;
  Tile clockTile;
  Tile currentlyPlayingTile;
  private VolumeTile volumeTile;

  public static void main(String[] args) throws Exception {
    launch(args);
  }

  public void start(final Stage stage) throws Exception {

    // Caption/title of the application
    stage.setTitle("Simple Media Player");

    final StackPane stackPane = new StackPane();
    BorderPane borderPane = new BorderPane();

    // determine the source directory for the play list (either the first
    // parameter to the program or the default path)
    final List<String> programParameters = getParameters().getRaw();

    // File that represents directory for the audio files
    // Uses the program parameters if one exists
    // Otherwise, uses default path (Public Music folder)
    final File sourceDirectory = (programParameters.size() > 0)
        ? new File(programParameters.get(0)) : new File(defaultPath);

    if (!sourceDirectory.exists() && sourceDirectory.isDirectory()) {
      LOGGER.info("Cannot find audio source directory: " + sourceDirectory);
    }
    if (!sourceDirectory.isDirectory()) {
      LOGGER.info(sourceDirectory + " is not a valid directory");
    }

    MenuBar menuBar = new MenuBar();

    // --- Menu File
    Menu menuFile = new Menu("File");
    // Sub menu
    Menu menuOpen = new Menu("Open");
    MenuItem openDirectoryChooser = new MenuItem("Open Directory");
    openDirectoryChooser.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
          LOGGER.severe("No Directory selected");
        } else {
          setMediaPlayers(selectedDirectory);
        }
      }
    });
    MenuItem openPlaylist = new MenuItem("Open Playlist");
    openPlaylist.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
          LOGGER.severe("No file selected");
        } else {
          openPlayList(selectedFile);
        }
      }
    });
    menuOpen.getItems().addAll(openPlaylist, openDirectoryChooser);
    MenuItem menuItemSaveAsPlayList = new MenuItem("Save As Playlist");
    menuItemSaveAsPlayList.setOnAction(new EventHandler<ActionEvent>() {
      // TODO Add way to choose save path
      @Override
      public void handle(ActionEvent event) {
        String defaulPathToSaveTo = "C:/Playlists/playlist.txt";
        playList.saveAsPlayList(defaulPathToSaveTo);
      }
    });
    menuFile.getItems().addAll(menuOpen, menuItemSaveAsPlayList);

    // --- Menu Edit
    Menu menuEdit = new Menu("Edit");

    // --- Menu Suggest
    Menu menuSuggest = new Menu("Suggest");

    menuBar.getMenus().addAll(menuFile, menuEdit, menuSuggest);
    menuBar.styleProperty().bind(Bindings.concat("-fx-font-size: 14"));

    /**
     * Invisible button as a template to get the actual preferred size of the
     * Pause button.
     **/
    Button invisiblePause = new Button("Pause");
    invisiblePause.setVisible(false);
    playNPause.prefHeightProperty().bind(invisiblePause.heightProperty());
    playNPause.prefWidthProperty().bind(invisiblePause.widthProperty());

    clockTile = TileBuilder.create().skinType(SkinType.CLOCK).title("Clock")
        .text("").dateVisible(true).locale(Locale.US).running(true).build();

    currentlyPlayingTile =
        TileBuilder.create().skinType(SkinType.TEXT).title("Currently Playing")
            .description("Currently Playing").textVisible(true).build();

    setMediaPlayers(sourceDirectory);

    HBox hBox = new HBox(10);
    hBox.setAlignment(Pos.CENTER);
    hBox.getChildren().addAll(next, playNPause, progress, mediaView,
        currentTime, slashPlaceHolder, totalTime);

    HBox tileBox = new HBox(10);
    tileBox.setAlignment(Pos.CENTER);
    tileBox.getChildren().addAll(currentlyPlayingTile, clockTile,
        volumeTile.getTile());

    VBox vBox = new VBox(20);
    vBox.getChildren().addAll(tileBox, hBox);

    stackPane.setAlignment(Pos.CENTER);
    stackPane.setCenterShape(true);
    stackPane.setPadding(new Insets(20));
    stackPane
        .setBackground(new Background(new BackgroundFill(Color.web("#101214"),
            CornerRadii.EMPTY, Insets.EMPTY)));
    stackPane.getChildren().addAll(invisiblePause, vBox);
    progress.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(progress, Priority.ALWAYS);
    stackPane.styleProperty().bind(Bindings.concat("-fx-font-size: 18"));

    borderPane.setCenter(stackPane);
    borderPane.setTop(menuBar);

    Scene scene = new Scene(borderPane, 700, 360);
    stage.getIcons().add(new Image(ICON_SOURCE_PATH));
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Sets the list of MediaPlayer by searching for files in the given directory
   * and creating a MediaPlayer for each file that ends with .mp3
   * 
   * @param sourceDirectory File that represents the directory to search through
   */
  private void setMediaPlayers(File sourceDirectory) {
    playList.stopAnyRunningTracks();
    playList.addAllFilesFromDirectoryIntoPlayList(sourceDirectory);
    setupMediaPlayerComponents();
  }

  /**
   * Sets up the MediaPlayer components (Buttons, Listeners, Song titles, etc.)
   */
  private void setupMediaPlayerComponents() {

    volumeTile = new VolumeTile(playList);

    // Create a MediaView to show the song title of the MediaPlayers
    mediaView = new MediaView(playList.getCurrentlyPlaying());

    playList.startupMediaPlayers(mediaView, progress, currentTime, totalTime,
        currentlyPlayingTile);
    playList.setCurrentMediaPlayer(mediaView, progress, currentTime, totalTime,
        currentlyPlayingTile, mediaView.getMediaPlayer());

    // Set action when clicking on Next button
    next.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        final MediaPlayer currentPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer = playList.getMediaPlayers()
            .get((playList.getMediaPlayers().indexOf(currentPlayer) + 1)
                % playList.getMediaPlayers().size());
        mediaView.setMediaPlayer(nextPlayer);
        currentPlayer.stop();
        nextPlayer.play();
        playList.setCurrentMediaPlayer(mediaView, progress, currentTime,
            totalTime, currentlyPlayingTile, nextPlayer);
        playNPause.setText("Pause");
      }
    });

    // Set action when clicking the Play/Pause button
    playNPause.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        // Pause action if current text of the button is "Pause"
        if ("Pause".equals(playNPause.getText())) {
          mediaView.getMediaPlayer().pause();
          playNPause.setText("Play");
        } else {
          mediaView.getMediaPlayer().play();
          playNPause.setText("Pause");
        }
      }
    });

  }

  private void openPlayList(File playListFile) {

    List<String> audioFiles = new ArrayList<String>();

    try (Scanner scanner = new Scanner(playListFile)) {
      while (scanner.hasNextLine()) {
        String audioFilePath = scanner.nextLine();
        audioFiles.add(audioFilePath);
      }
    } catch (FileNotFoundException fnfe) {
      Logger.getGlobal().severe("A FileNotFoundException has occured: " + fnfe);
      fnfe.printStackTrace();
    }

    playList.stopAnyRunningTracks();
    playList.addAllFilesIntoPlayList(audioFiles);
    setupMediaPlayerComponents();
  }

  /**
   * @return the defaultPath
   */
  public String getDefaultPath() {
    return defaultPath;
  }

  /**
   * @param defaultPath the defaultPath to set
   */
  public void setDefaultPath(String defaultPath) {
    this.defaultPath = defaultPath;
  }

  /**
   * @return the playList
   */
  public PlayList getPlayList() {
    return playList;
  }

  /**
   * @param playList the playList to set
   */
  public void setPlayList(PlayList playList) {
    this.playList = playList;
  }

  /**
   * @return the lOGGER
   */
  public Logger getLOGGER() {
    return LOGGER;
  }

  /**
   * @param lOGGER the lOGGER to set
   */
  public void setLOGGER(Logger lOGGER) {
    LOGGER = lOGGER;
  }

  /**
   * @return the currentlyPlaying
   */
  public Label getCurrentlyPlaying() {
    return currentlyPlaying;
  }

  /**
   * @param currentlyPlaying the currentlyPlaying to set
   */
  public void setCurrentlyPlaying(Label currentlyPlaying) {
    this.currentlyPlaying = currentlyPlaying;
  }

  /**
   * @return the progress
   */
  public ProgressBar getProgress() {
    return progress;
  }

  /**
   * @param progress the progress to set
   */
  public void setProgress(ProgressBar progress) {
    this.progress = progress;
  }

  /**
   * @return the mediaView
   */
  public MediaView getMediaView() {
    return mediaView;
  }

  /**
   * @param mediaView the mediaView to set
   */
  public void setMediaView(MediaView mediaView) {
    this.mediaView = mediaView;
  }

  /**
   * @return the next
   */
  public Button getNext() {
    return next;
  }

  /**
   * @param next the next to set
   */
  public void setNext(Button next) {
    this.next = next;
  }

  /**
   * @return the playNPause
   */
  public Button getPlayNPause() {
    return playNPause;
  }

  /**
   * @param playNPause the playNPause to set
   */
  public void setPlayNPause(Button playNPause) {
    this.playNPause = playNPause;
  }

  /**
   * @return the currentTime
   */
  public Label getCurrentTime() {
    return currentTime;
  }

  /**
   * @param currentTime the currentTime to set
   */
  public void setCurrentTime(Label currentTime) {
    this.currentTime = currentTime;
  }

  /**
   * @return the totalTime
   */
  public Label getTotalTime() {
    return totalTime;
  }

  /**
   * @param totalTime the totalTime to set
   */
  public void setTotalTime(Label totalTime) {
    this.totalTime = totalTime;
  }

  /**
   * @return the twoDigitFormat
   */
  public DecimalFormat getTwoDigitFormat() {
    return twoDigitFormat;
  }

  /**
   * @param twoDigitFormat the twoDigitFormat to set
   */
  public void setTwoDigitFormat(DecimalFormat twoDigitFormat) {
    this.twoDigitFormat = twoDigitFormat;
  }

  /**
   * @return the iconSourcePath
   */
  public static String getIconSourcePath() {
    return ICON_SOURCE_PATH;
  }

  /**
   * @return the slashPlaceHolder
   */
  public Label getSlashPlaceHolder() {
    return slashPlaceHolder;
  }
}
