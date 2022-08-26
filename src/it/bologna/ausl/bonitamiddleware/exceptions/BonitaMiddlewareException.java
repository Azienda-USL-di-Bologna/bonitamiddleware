package it.bologna.ausl.bonitamiddleware.exceptions;

/**
 *
 * @author gdm
 */
public class BonitaMiddlewareException extends Exception{

  /**
   *
   */
  public BonitaMiddlewareException() {
  }


  /**
   * @param message
   */
  public BonitaMiddlewareException(String message) {
    super(message);
  }


  /**
   * @param cause
   */
  public BonitaMiddlewareException(Throwable cause) {
    super(cause);
  }


  /**
   * @param message
   * @param cause
   */
  public BonitaMiddlewareException(String message, Throwable cause) {
    super(message, cause);
  }
}
