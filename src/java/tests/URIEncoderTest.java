package tests;

import org.testng.annotations.Test;

import simplemediaplayer.URIEncoder;
import utils.VerificationUtils;

public class URIEncoderTest {
  private static String URIString = "%60" + "a" + "%2d" + "bc" + "%20" + "%25" + "d" + "%2d" + "ef"
      + "%20" + "%25" + "g" + "%2d" + "hi" + "%5B" + "%23" + "%5D";

  private static String standardString = "`" + "a" + "-" + "bc" + " " + "%" + "d" + "-" + "ef" + " "
      + "%" + "g" + "-" + "hi" + "[" + "#" + "]";

  private static String emptyString = "";


  @Test
  public void testURIEncoder() {
    VerificationUtils.verify(URIEncoder.encodeURI(standardString), URIString,
        "URI Encoding standard string");
  }

  @Test
  public void testURIDecoder() {
    VerificationUtils.verify(URIEncoder.decodeURI(URIString), standardString,
        "URI Decoding standard string");
  }

  @Test
  public void testURIEncoderEmptyString() {
    VerificationUtils.verify(URIEncoder.encodeURI(emptyString), emptyString,
        "URI Encoding empty string");
  }

  @Test
  public void testURIDecoderEmptyString() {
    VerificationUtils.verify(URIEncoder.decodeURI(emptyString), emptyString,
        "URI Decoding empty string");
  }
}
