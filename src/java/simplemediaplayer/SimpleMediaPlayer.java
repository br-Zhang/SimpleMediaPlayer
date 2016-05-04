package simplemediaplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A Simple Media Player that plays audio files from a directory or PlayList and allows saving
 * PlayLists into a text file.
 * 
 * @author Brendan Zhang
 * @created 2016-04-24
 * @since 2016-05-04
 */
public class SimpleMediaPlayer extends Application {

  PlayList playList = new PlayList();
  // Logger to log information messages to
  Logger LOGGER = Logger.getGlobal();
  // Label to display title of currently playing audio file
  private Label currentlyPlaying = new Label();
  // ProgressBar to show progress of song
  private ProgressBar progress = new ProgressBar();
  // Slider to adjust volume of MediaPlayers
  private Slider volumeSlider = new Slider();
  // ChangeListener to listen to progress of currently playing MediaPlayer
  private ChangeListener<Duration> progressChangeListener;
  // Path to icon of the application
  private final static String ICON_SOURCE_PATH = "images/windows_media_player.png";
  // MediaView to show the song title of the currently playing MediaPlayer
  private MediaView mediaView;
  // "Next" button
  private Button next = new Button("Next");
  // Button that will be used for "Play" and "Pause
  private Button playNPause = new Button("Pause");
  // Default volume set to 50
  private Number volume = 50;
  private final Label slashPlaceHolder = new Label("/");
  private Label currentTime = new Label();
  private Label totalTime = new Label();
  DecimalFormat twoDigitFormat = new DecimalFormat("00");

  public static void main(String[] args) throws Exception {
    launch(args);
  }

  public void start(final Stage stage) throws Exception {

    // Default path of the audio files to load
    String defaultPath = "C:\\Users\\Public\\Music";

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
    final File sourceDirectory =
        (programParameters.size() > 0) ? new File(programParameters.get(0)) : new File(defaultPath);

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

    setMediaPlayers(sourceDirectory);

    /**
     * Silly invisible button used as a template to get the actual preferred size of the Pause
     * button.
     **/
    Button invisiblePause = new Button("Pause");
    invisiblePause.setVisible(false);
    playNPause.prefHeightProperty().bind(invisiblePause.heightProperty());
    playNPause.prefWidthProperty().bind(invisiblePause.widthProperty());

    // Set slider properties
    volumeSlider.setMin(0);
    volumeSlider.setMax(100);
    volumeSlider.setValue(50);
    volumeSlider.setShowTickLabels(true);
    volumeSlider.setShowTickMarks(true);
    volumeSlider.setMajorTickUnit(50);
    volumeSlider.setMinorTickCount(5);
    volumeSlider.setBlockIncrement(10);

    HBox hBox = new HBox(10);
    hBox.setAlignment(Pos.CENTER);
    hBox.getChildren().addAll(next, playNPause, progress, mediaView, currentTime, slashPlaceHolder,
        totalTime);

    VBox vBox = new VBox(20);
    vBox.getChildren().addAll(currentlyPlaying, hBox, volumeSlider);

    stackPane.setStyle("-fx-background-color: LIGHTSTEELBLUE; " + "-fx-font-size: 20; "
        + "-fx-padding: 20; " + "-fx-alignment: center;");
    stackPane.getChildren().addAll(invisiblePause, vBox);
    progress.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(progress, Priority.ALWAYS);

    borderPane.setCenter(stackPane);
    borderPane.setTop(menuBar);

    Scene scene = new Scene(borderPane, 600, 270);
    stage.getIcons().add(new Image(ICON_SOURCE_PATH));
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Sets the Currently Playing label to the file name of the audio file of the new media player and
   * updates the progress monitor.
   * 
   * @param newPlayer The MediaPlayer to set the progress monitor to listen to and to set the
   *        Current Playing label to
   */
  private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
    progress.setProgress(0);
    progressChangeListener = new ChangeListener<Duration>() {
      @Override
      public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue,
          Duration newValue) {
        progress.setProgress(
            1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());

        String currentDurationMinutes =
            twoDigitFormat.format(Math.floor(newPlayer.getCurrentTime().toMinutes()));
        String currentDurationSeconds = twoDigitFormat.format(Math.round(
            newPlayer.getCurrentTime().toSeconds() - Long.parseLong(currentDurationMinutes) * 60));

        String totalDurationMinutes =
            twoDigitFormat.format(Math.floor(newPlayer.getTotalDuration().toMinutes()));
        String totalDurationSeconds = twoDigitFormat.format(Math.round(
            newPlayer.getTotalDuration().toSeconds() - Long.parseLong(totalDurationMinutes) * 60));

        // Workaround for seconds hitting 60 instead of reseting
        if (currentDurationSeconds.equals("60")) {
          currentDurationSeconds = "00";
          currentDurationMinutes =
              twoDigitFormat.format(Math.floor(newPlayer.getCurrentTime().toMinutes() + 1));
        }
        if (totalDurationSeconds.equals("60")) {
          totalDurationSeconds = "00";
          totalDurationMinutes =
              twoDigitFormat.format(Math.floor(newPlayer.getCurrentTime().toMinutes() + 1));
        }

        currentTime.setText(currentDurationMinutes + ":" + currentDurationSeconds);
        totalTime.setText(totalDurationMinutes + ":" + totalDurationSeconds);
      }
    };

    newPlayer.currentTimeProperty().addListener(progressChangeListener);

    String source = newPlayer.getMedia().getSource();
    source = source.substring(0, source.length() - ".mp3".length());

    // Since we modified the source of the audio files to remove annoying to
    // handle characters, re-add them back.
    source = PlayList.modifyFixedPathToTitle(source);
    source = source.substring(source.lastIndexOf("/") + 1);

    currentlyPlaying.setText("Currently Playing: " + source);
  }

  /**
   * Sets the volume of the MediaPlayers in {@code mediaPlayer}.
   * 
   * @param volumeToSetTo The volume to set to
   */
  private void setVolume(Number volumeToSetTo) {
    volume = volumeToSetTo;

    for (MediaPlayer mediaPlayer : playList.getMediaPlayers()) {
      mediaPlayer.setVolume(volumeToSetTo.doubleValue() / 100);
    }
  }

  /**
   * Sets the list of MediaPlayer by searching for files in the given directory and creating a
   * MediaPlayer for each file that ends with .mp3
   * 
   * @param sourceDirectory File that represents the directory to search through
   */
  private void setMediaPlayers(File sourceDirectory) {
    playList.stopAnyRunningTracks(mediaView);
    playList.addAllFilesFromDirectoryIntoPlayList(sourceDirectory);
    setupMediaPlayerComponents();
  }

  /**
   * Sets up the MediaPlayer components (Buttons, Listeners, Song titles, etc.)
   */
  private void setupMediaPlayerComponents() {

    // Create a MediaView to show the song title of the MediaPlayers
    mediaView = new MediaView(playList.getCurrentlyPlaying());

    // Start playing the first track.
    mediaView.setMediaPlayer(playList.getCurrentlyPlaying());
    mediaView.getMediaPlayer().play();
    setCurrentlyPlaying(mediaView.getMediaPlayer());

    playList.startupMediaPlayers(mediaView, progressChangeListener);

    // Set action when clicking on Next button
    next.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        final MediaPlayer currentPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer =
            playList.getMediaPlayers().get((playList.getMediaPlayers().indexOf(currentPlayer) + 1)
                % playList.getMediaPlayers().size());
        mediaView.setMediaPlayer(nextPlayer);
        currentPlayer.currentTimeProperty().removeListener(progressChangeListener);
        currentPlayer.stop();
        nextPlayer.play();
      }
    });

    // Set action when changing Volume control slider
    volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
        setVolume(new_val);
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

    // Display the name of the currently playing track.
    mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
      @Override
      public void changed(ObservableValue<? extends MediaPlayer> observableValue,
          MediaPlayer oldPlayer, MediaPlayer newPlayer) {
        setCurrentlyPlaying(newPlayer);
      }
    });

    // Sets the volume of the MediaPlayers.
    // This is to carry over any previous volume changes while
    // maintaining default of 50.
    setVolume(volume);


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

    playList.stopAnyRunningTracks(mediaView);
    playList.addAllFilesIntoPlayList(audioFiles);
    setupMediaPlayerComponents();
  }
}
