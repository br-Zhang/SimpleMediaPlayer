package simplemediaplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * Class to represent Playlists.
 * 
 * @author Brendan Zhang
 */
public class PlayList {

  /**
   * TODO Put the MediaPlayer/Playlist related functions/code in here and seperate that from the
   * SimpleMediaPlayer class.
   */

  private List<MediaPlayer> mediaPlayers = new ArrayList<MediaPlayer>();

  private List<String> filePaths;

  public PlayList() {
    this.setFilePaths(new ArrayList<String>());
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
      mediaPlayers.add(this.createMediaPlayerFromFile(
          filePrefix + modifyPathToFixedPath((sourceDirectory + "\\" + file).replace("\\", "/"))));
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
    this.stopAnyRunningTracks(mediaView);
    // shuffle the file paths
    for (int i = 0; i < filePaths.size(); ++i) {
      Random rand = new Random();
      int temp = rand.nextInt(filePaths.size() - i) + i;
      Collections.swap(filePaths, i, temp);
    }
    this.addAllFilesIntoPlayList(filePaths);
  }

  /**
   * Stops any currently running MediaPlayer and clears the current list of MediaPlayers.
   * 
   * @param mediaView The MediaView associated with this PlayList's MediaPlayer
   */
  private void stopAnyRunningTracks(MediaView mediaView) {
    // Stop any currently running MediaPlayer
    if (mediaPlayers.size() > 0) {
      mediaView.getMediaPlayer().stop();
      mediaPlayers = new ArrayList<MediaPlayer>();
    }
  }

  /**
   * Creates a MediaPlayer from the given path to the audio file. If an error occurs, it will log
   * it.
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
      PrintWriter printWriter =
          new PrintWriter(new FileWriter(fileCurrentPlayList.getAbsolutePath()));
      for (String filePath : filePaths) {
        printWriter.println(filePath);
      }
      printWriter.close();
    } catch (IOException ioe) {
      Logger.getGlobal().severe("An I/O Exception occurred: " + ioe);
      ioe.printStackTrace();
    }
  }

  public void startupMediaPlayers(MediaView mediaView,
      ChangeListener<Duration> progressChangeListener) {
    // Play each audio file
    for (int i = 0; i < mediaPlayers.size(); i++) {
      MediaPlayer currentPlayer = mediaPlayers.get(i);
      // Get next player (and account for looping)
      MediaPlayer nextPlayer = mediaPlayers.get((i + 1) % mediaPlayers.size());
      currentPlayer.setOnEndOfMedia(new Runnable() {
        @Override
        public void run() {
          // Remove progress listener from current player
          getCurrentPlayer().currentTimeProperty().removeListener(progressChangeListener);
          // Set view to show song title of next song
          mediaView.setMediaPlayer(nextPlayer);
          // Play next song
          nextPlayer.play();
        }
      });
    }
  }

  public MediaPlayer getCurrentPlayer() {
    return this.getMediaPlayers().get(0);
  }

  /**
   * Convenience method used to change a strict file path to a URI encoded path (fixing the path to
   * match URI specifications).
   * 
   * @param path String that contains the file path
   * @return The file path with URI encoding
   */
  private static String modifyPathToFixedPath(String path) {
    return URIEncoder.encodeURI(path);
  }

  private static String modifyFixedPathToTitle(String fixedPath) {
    return URIEncoder.decodeURI(fixedPath);
  }
}
