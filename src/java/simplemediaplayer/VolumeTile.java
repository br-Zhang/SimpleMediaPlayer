package simplemediaplayer;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.MediaPlayer;

public class VolumeTile {
  Tile sliderTile;
  PlayList playList;

  public VolumeTile(final PlayList playList) {
    this.playList = playList;
    sliderTile = TileBuilder.create().skinType(SkinType.SLIDER).title("Volume")
        .unit("%").barBackgroundColor(Tile.FOREGROUND).build();
    // Default value
    sliderTile.setValue(50);
    setVolume(sliderTile.getValue(), playList);
    sliderTile.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable,
          Number oldValue, Number newValue) {
        setVolume(newValue, playList);
      }
    });

  }

  /**
   * Sets the volume of the MediaPlayers in {@code mediaPlayer}.
   * 
   * @param volumeToSetTo The volume to set to
   */
  private void setVolume(Number volumeToSetTo, PlayList playList) {
    for (MediaPlayer mediaPlayer : playList.getMediaPlayers()) {
      mediaPlayer.setVolume(volumeToSetTo.doubleValue() / 100);
    }
  }

  public Tile getTile() {
    return sliderTile;
  }
}
