package utils;

import java.util.List;

import org.testng.Assert;

public class VerificationUtils {
  public static boolean verify(List<String> actual, List<String> expected, String msg) {
    // Handle nulls
    if (expected == null && actual == null) {
      return true;
    }
    if (expected == null || actual == null) {
      return false;
    }
    // Quick size check
    if (expected.size() != actual.size()) {
      return false;
    }
    for (int i = 0; i < expected.size(); i++) {
      if (!actual.get(i).equals(expected.get(i))) {
        Assert.fail("Verification of " + msg + " failed. \n" + "Expected: '" + expected.get(i)
            + "' but received: '" + actual.get(i) + "'");
        return false;
      }
    }
    return true;
  }

  public static boolean verify(Object actual, Object expected, String msg) {
    // Handle nulls
    if (expected == null && actual == null) {
      return true;
    }
    if (expected == null || actual == null) {
      return false;
    }

    if (!actual.equals(expected)) {
      Assert.fail("Verification of " + msg + " failed. \n" + "Expected: '" + expected.toString()
          + "' but received: '" + actual.toString() + "'");
      return false;
    }

    return true;
  }
}
