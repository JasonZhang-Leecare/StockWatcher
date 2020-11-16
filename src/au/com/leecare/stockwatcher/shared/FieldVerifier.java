package au.com.leecare.stockwatcher.shared;

/** FieldVerifier validates that the name the user enters is valid. */
public class FieldVerifier {

  /**
   * Verifies that the specified name is valid for our service.
   *
   * @param name the name to validate
   * @return true if valid, false if invalid
   */
  public static boolean isValidName(String name) {
    if (name == null) {
      return false;
    }
    return name.length() > 3;
  }
}
