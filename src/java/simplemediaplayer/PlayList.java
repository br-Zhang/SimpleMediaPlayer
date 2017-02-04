package simplemediaplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import eu.hansolo.tilesfx.Tile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * Class to represent the Playlists used in {@link SimpleMediaPlayer}.
 * 
 * @author Brendan Zhang
 * @since 2016-05-04
 */
public class PlayList {

  /**
   * TODO Put the MediaPlayer/Playlist related functions/code in here and
   * seperate that from the SimpleMediaPlayer class.
   */

  private DecimalFormat twoDigitFormat = new DecimalFormat("00");

  private String playListID;
  private String playListName;
  private List<MediaPlayer> mediaPlayers = new ArrayList<MediaPlayer>();
  // ChangeListener to listen to progress of currently playing MediaPlayer
  private ChangeListener<Duration> progressChangeListener;
  private List<String> filePaths;

  public PlayList() {
    this.setFilePaths(new ArrayList<String>());
  }

  public PlayList(String playListID, String playListName) {
    this.playListID = playListID;
    this.playListName = playListName;
  }

  public static PlayList openPlayList(File playListFile) {
    PlayList playList = new PlayList();

    try (Scanner scanner = new Scanner(playListFile)) {
      while (scanner.hasNextLine()) {
        String audioFilePath = scanner.nextLine();
        playList.getFilePaths().add(audioFilePath);
      }
    } catch (FileNotFoundException fnfe) {
      Logger.getGlobal().severe("A FileNotFoundException has occured: " + fnfe);
      // TODO Auto-generated catch block
      fnfe.printStackTrace();
    }
    return playList;
  }

  /**
   * @return the playListID
   */
  public String getPlayListID() {
    return playListID;
  }

  /**
   * @param playListID the playListID to set
   */
  public void setPlayListID(String playListID) {
    this.playListID = playListID;
  }

  /**
   * @return the playListName
   */
  public String getPlayListName() {
    return playListName;
  }

  /**
   * @param playListName the playListName to set
   */
  public void setPlayListName(String playListName) {
    this.playListName = playListName;
  }

  public List<MediaPlayer> getMediaPlayers() {
    return mediaPlayers;
  }

  public void setMediaPlayers(List<MediaPlayer> mediaPlayers) {
    this.mediaPlayers = mediaPlayers;
  }

  /**
   * @return The file paths of the audio files of the PlayList
   */
  public List<String> getFilePaths() {
    return filePaths;
  }

  /**
   * @param filePaths The file paths of the audio files to set to
   */
  public void setFilePaths(List<String> filePaths) {
    this.filePaths = filePaths;
  }

  /**
   * @param filePaths The file paths of the audio files to set to
   */
  public void setFilePaths(String[] filePaths) {
    this.filePaths = new ArrayList<String>();
    this.filePaths.addAll(Arrays.asList(filePaths));
  }

  /**
   * Assumes are filePaths are fixed already.
   * 
   * @param filePaths
   */
  public void addAllFilesIntoPlayList(List<String> filePaths) {

    for (String filePath : filePaths) {
      mediaPlayers.add(this.createMediaPlayerFromFile(filePath));
    }
  }

  /**
   * Add all files from the given directory into the PlayList.
   * 
   * @param sourceDirectory
   */
  public void addAllFilesFromDirectoryIntoPlayList(File sourceDirectory) {
    String filePrefix = "file:///";

    for (String file : sourceDirectory.list(new FilenameFilter() {
      @Override
      public boolean accept(File file, String name) {
        return name.endsWith(".mp3");
      }
    })) {
      String filePath = filePrefix + modifyPathToFixedPath(
          (sourceDirectory + "\\" + file).replace("\\", "/"));
      filePaths.add(filePath);
      mediaPlayers.add(this.createMediaPlayerFromFile(filePath));
    }

    if (mediaPlayers.isEmpty()) {
      Logger.getGlobal().severe("No audio found in " + sourceDirectory);
      throw new RuntimeException("No audio found in " + sourceDirectory);
    }
  }

  /**
   * Shuffles the current playlist
   */
  public void shuffle(MediaView mediaView) {
    this.stopAnyRunningTracks();
    // shuffle the file paths
    for (int i = 0; i < filePaths.size(); ++i) {
      Random rand = new Random();
      int temp = rand.nextInt(filePaths.size() - i) + i;
      Collections.swap(filePaths, i, temp);
    }
    this.addAllFilesIntoPlayList(filePaths);
  }

  /**
   * Stops any currently running MediaPlayer and clears the current list of
   * MediaPlayers.
   * 
   * @param mediaView The MediaView associated with this PlayList's MediaPlayer
   */
  public void stopAnyRunningTracks() {
    // Stop any currently running MediaPlayer
    if (mediaPlayers.size() > 0) {
      if (progressChangeListener != null) {
        getCurrentlyPlaying().currentTimeProperty()
            .removeListener(progressChangeListener);
      }
      getCurrentlyPlaying().stop();
      mediaPlayers = new ArrayList<MediaPlayer>();
      progressChangeListener = null;
      filePaths = new ArrayList<String>();
    }
  }

  /**
   * Creates a MediaPlayer from the given path to the audio file. If an error
   * occurs, it will log it.
   * 
   * @param sourcePath The full path to the audio file
   * @return MediaPlayer for the given audio file
   */
  private MediaPlayer createMediaPlayerFromFile(String sourcePath) {
    final MediaPlayer player = new MediaPlayer(new Media(sourcePath));
    player.setOnError(new Runnable() {
      @Override
      public void run() {
        Logger.getGlobal().info("Media error occurred: " + player.getError());
      }
    });
    return player;
  }

  /**
   * Saves the current file paths as a PlayList.
   * 
   * @param pathToSavePlayListTo Path to save the playlist.txt to
   */
  public void saveAsPlayList(String pathToSavePlayListTo) {
    System.out.println(this.toString());
    File fileCurrentPlayList = new File(pathToSavePlayListTo);
    if (!fileCurrentPlayList.exists()) {
      fileCurrentPlayList.getParentFile().mkdirs();
      try {
        fileCurrentPlayList.createNewFile();
      } catch (IOException ioe) {
        Logger.getGlobal().severe("An I/O Exception occurred: " + ioe);
        ioe.printStackTrace();
      }
    }
    try {
      PrintWriter printWriter = new PrintWriter(
          new FileWriter(fileCurrentPlayList.getAbsolutePath()));
      for (String filePath : filePaths) {
        printWriter.println(filePath);
      }
      printWriter.close();
    } catch (IOException ioe) {
      Logger.getGlobal().severe("An I/O Exception occurred: " + ioe);
      ioe.printStackTrace();
    }
  }

  public void startupMediaPlayers(final MediaView mediaView,
      final ProgressBar progress, final Label currentTime,
      final Label totalTime, final Tile currentlyPlayingTile) {

    // Play each audio file
    for (int i = 0; i < mediaPlayers.size(); i++) {
      final MediaPlayer currentPlayer = mediaPlayers.get(i);

      // Get next player (and account for looping)
      final MediaPlayer nextPlayer =
          mediaPlayers.get((i + 1) % mediaPlayers.size());

      currentPlayer.setOnEndOfMedia(new Runnable() {
        @Override
        public void run() {
          setCurrentMediaPlayer(mediaView, progress, currentTime, totalTime,
              currentlyPlayingTile, nextPlayer);
        }
      });
    }
  }

  /**
   * Returns the currently playing MediaPlayer. Returns null iff the
   * mediaPlayers list is empty.
   * 
   * @return currently playing MediaPlayer, if one exists. Otherwise, return
   *         null.
   */
  public MediaPlayer getCurrentlyPlaying() {
    if (this.getMediaPlayers().size() == 0) {
      return null;
    } else {
      return this.getMediaPlayers().get(0);
    }
  }

  /**
   * Convenience method used to change a strict file path to a URI encoded path
   * (fixing the path to match URI specifications).
   * 
   * @param path String that contains the file path
   * @return The file path with URI encoding
   */
  public static String modifyPathToFixedPath(String path) {
    return URIEncoder.encodeURI(path);
  }

  public static String modifyFixedPathToTitle(String fixedPath) {
    return URIEncoder.decodeURI(fixedPath);
  }

  /**
   * Prints a string representation of the PlayList.
   * 
   * @return String that contains "Playlist:" followed by each of the file paths
   *         of the files in the PlayList.
   * 
   */
  public String toString() {
    String output = "PlayList:\n";
    for (String file : filePaths) {
      output += file + "\n";
    }
    return output;
  }

  /**
   * Sets the Currently Playing label to the file name of the audio file of the
   * new media player and updates the progress monitor.
   * 
   * @param newPlayer The MediaPlayer to set the progress monitor to listen to
   *        and to set the Current Playing label to
   */
  public void setCurrentMediaPlayer(final MediaView mediaView,
      final ProgressBar progress, final Label currentTime,
      final Label totalTime, Tile currentlyPlayingTile,
      final MediaPlayer newPlayer) {

    final MediaPlayer currentPlayer = mediaView.getMediaPlayer();

    if (currentPlayer != null) {
      currentPlayer.stop();
      if (progressChangeListener != null) {
        currentPlayer.currentTimeProperty()
            .removeListener(progressChangeListener);
      }
      if (newPlayer != getCurrentlyPlaying()) {
        // Move playlist down one
        this.getMediaPlayers().add(this.getMediaPlayers().remove(0));
      }
    }

    mediaView.setMediaPlayer(newPlayer);
    newPlayer.play();

    progress.setProgress(0);
    this.progressChangeListener = new ChangeListener<Duration>() {
      @Override
      public void changed(ObservableValue<? extends Duration> observableValue,
          Duration oldValue, Duration newValue) {

        progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis()
            / newPlayer.getTotalDuration().toMillis());

        String currentDurationMinutes = twoDigitFormat
            .format(Math.floor(newPlayer.getCurrentTime().toMinutes()));
        String currentDurationSeconds = twoDigitFormat
            .format(Math.round(newPlayer.getCurrentTime().toSeconds()
                - Long.parseLong(currentDurationMinutes) * 60));

        String totalDurationMinutes = twoDigitFormat
            .format(Math.floor(newPlayer.getTotalDuration().toMinutes()));
        String totalDurationSeconds = twoDigitFormat
            .format(Math.round(newPlayer.getTotalDuration().toSeconds()
                - Long.parseLong(totalDurationMinutes) * 60));

        // Workaround for seconds hitting 60 instead of reseting
        if (currentDurationSeconds.equals("60")) {
          currentDurationSeconds = "00";
          currentDurationMinutes = twoDigitFormat
              .format(Math.floor(newPlayer.getCurrentTime().toMinutes() + 1));
        }
        if (totalDurationSeconds.equals("60")) {
          totalDurationSeconds = "00";
          totalDurationMinutes = twoDigitFormat
              .format(Math.floor(newPlayer.getCurrentTime().toMinutes() + 1));
        }

        currentTime
            .setText(currentDurationMinutes + ":" + currentDurationSeconds);
        totalTime.setText(totalDurationMinutes + ":" + totalDurationSeconds);
      }
    };

    newPlayer.currentTimeProperty().addListener(progressChangeListener);

    setCurrentlyPlayingText(currentlyPlayingTile);
  }

  public void setCurrentlyPlayingText(Tile currentlyPlayingTile) {

    String source = getCurrentlyPlaying().getMedia().getSource();
    source = source.substring(0, source.length() - ".mp3".length());

    // Since we modified the source of the audio files to remove annoying to
    // handle characters, re-add them back.
    source = PlayList.modifyFixedPathToTitle(source);
    source = source.substring(source.lastIndexOf("/") + 1);

    currentlyPlayingTile.setDescription(source);

    System.out.println(currentlyPlayingTile.getDescription());
  }
}
