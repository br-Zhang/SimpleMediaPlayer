package simplemediaplayer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * Simple URI encoder/decoder class.
 * 
 * @author Brendan Zhang
 * @since 2016-05-02
 */
public class URIEncoder {

    /**
     * Encodes the given string into URI format.
     * 
     * @param argString
     *            String to encode to URI format
     * @return The string in URI format
     */
    public static String encodeURI(String argString) {
	String result = "";
	try {
	    result = URLEncoder.encode(argString, "UTF-8")
		    .replaceAll("\\+", "%20").replaceAll("\\%21", "!")
		    .replaceAll("\\%27", "'").replaceAll("\\%28", "(")
		    .replaceAll("\\%29", ")").replaceAll("\\%7E", "~")
		    .replaceAll("-", "\\%2d");
	} catch (UnsupportedEncodingException uee) {
	    Logger.getGlobal().severe(
		    "An UnsupportedEncodingException occurred: " + uee);
	    uee.printStackTrace();
	}
	return result;
    }

    /**
     * Decodes the given URI String to a normal UTF-8 String.
     * 
     * @param uriString
     *            URI String to decode
     * @return The string as UTF-8
     */
    public static String decodeURI(String uriString) {
	String output = "";
	try {
	    output = java.net.URLDecoder.decode(uriString, "UTF-8");
	} catch (UnsupportedEncodingException uee) {
	    Logger.getGlobal().severe(
		    "An UnsupportedEncodingException occurred: " + uee);
	    uee.printStackTrace();
	}
	return output;
    }
}
