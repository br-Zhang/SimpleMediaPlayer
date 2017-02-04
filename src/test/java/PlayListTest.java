
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import simplemediaplayer.PlayList;
import utils.VerificationUtils;

/**
 * Simple tests that verify the functions of the {@link PlayList} class.
 * <p>
 * The following features are tested:
 * <ul>
 * <li>Saving a file in PlayList format</li>
 * <li>Opening a file in PlayList format</li>
 * <li>Save As a PlayList</li>
 * </ul>
 * 
 * @author Brendan Zhang
 * @since 2016-05-04
 *
 */
public class PlayListTest {

  public static String playListFile = "C:\\PlayLists\\playlist.txt";
  public static String[] filePaths = {"C:/Madeup.mp3", "D:/Test.mp3"};
  public static String[] saveAsFilePaths = {"T3$7!4LL'.mp3", "X:/Whatup/y.mp3"};

  @Test(priority = 1)
  public void testSavingPlayList() {
    File fileCurrentPlayList = new File(playListFile);
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

  @Test(priority = 2)
  public void testOpeningPlayList() {
    PlayList playList = PlayList.openPlayList(new File(playListFile));
    List<String> expectedFilePaths = new ArrayList<String>();
    expectedFilePaths.addAll(Arrays.asList(filePaths));
    VerificationUtils.verify(playList.getFilePaths(), expectedFilePaths,
        "File path of the PlayList");
  }

  @Test(priority = 3)
  public void testSaveAsPlayList() {
    PlayList playList = new PlayList();
    playList.setFilePaths(saveAsFilePaths);
    playList.saveAsPlayList(playListFile);
    PlayList playListTwo = PlayList.openPlayList(new File(playListFile));
    VerificationUtils.verify(playListTwo.getFilePaths(),
        Arrays.asList(saveAsFilePaths), "Save As File paths");
  }

  @Test
  public void testToStringPlayList() {
    PlayList playList = new PlayList();
    VerificationUtils.verify(playList.toString(), "PlayList:\n",
        "Empty PlayList toString");
  }

  @Test
  public void testToStringPlayListRemoveElement() {
    PlayList playList = new PlayList();
    playList.setFilePaths(filePaths);
    playList.getFilePaths().remove(0);
    VerificationUtils.verify(playList.toString(), "PlayList:\nD:/Test.mp3\n",
        "Removed Element PlayList toString");
  }
}
