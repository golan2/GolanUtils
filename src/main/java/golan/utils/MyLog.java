package golan.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <pre>
 * <B>Copyright:</B>   HP Software IL
 * <B>Owner:</B>       <a href="mailto:izik.golan@hp.com">Izik Golan</a>
 * <B>Creation:</B>    13/03/2015 08:29
 * <B>Since:</B>       BSM 9.21
 * <B>Description:</B>
 *
 * </pre>
 */
public class MyLog {

  private static final SimpleDateFormat  sdf        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.US);
  private static final GregorianCalendar calendar   = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
  private static final String            CLASS_NAME = MyLog.class.getCanonicalName();
  private static final String            CATEGORY   = "Izik";

  public void error(String s, Throwable t) {
    log(LogLevel.ERROR, s, t);
  }

  public enum LogLevel {ERROR, WARN, INFO, DEBUG}


  private static String generatePrefix(LogLevel logLevel) {
    StringBuilder buf = new StringBuilder();
    buf.append("[").append(getCurrentDateAndTime()).append("] ");
    buf.append("[").append(padRight(logLevel.toString(), 7)).append("] ");
    buf.append("[").append(Thread.currentThread().getName()).append("] ");
    buf.append("[").append(getFileNameAndLineNumber()).append("] \t");
    return  buf.toString();
  }


  /**
   * The [stackTrace] will contain a few callers but eventually we will have "Logger.java" there.
   * So we iterate the [stackTrace] in order to find it.     *
   * But it might appear more than once (once for generatePrefix and once for log) so we iterate all "Logger.java" until we find the next one
   * This next one is the place in the [stackTrace] who is the caller to [log]
   * @return the file name and the line number of the caller
   */
  private static String getFileNameAndLineNumber() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    boolean found = false;
    for (StackTraceElement ste : stackTrace) {
      if (CLASS_NAME.equals(ste.getClassName())) {
        found = true;
      } else {
        if (found) {
          return ste.getFileName() + ":" + ste.getLineNumber();
        }
      }
    }
    return "?";
  }

  private static String getCurrentDateAndTime() {
    calendar.setTimeInMillis(System.currentTimeMillis());
    return sdf.format(calendar.getTime());
  }

  public static void log(Throwable t) {
    log(LogLevel.ERROR, toStackTrace(t));
  }

  private static String toStackTrace(Throwable t) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream s = new PrintStream(out);
    t.printStackTrace(s);
    return out.toString();
  }

  public static void log(LogLevel logLevel, String message, Throwable t) {

    log(logLevel, message + "\n" + toStackTrace(t));
  }

  public static void log(LogLevel logLevel, String message) {

    System.out.println(generatePrefix(logLevel) + message);
    //if (LogLevel.DEBUG==logLevel) {
    //  Log.d(CATEGORY, msg);
    //}
    //else if (LogLevel.INFO==logLevel) {
    //  Log.i(CATEGORY, msg);
    //}
    //else if (LogLevel.WARN==logLevel) {
    //  Log.w(CATEGORY, msg);
    //}
    //else if (LogLevel.ERROR==logLevel) {
    //  Log.e(CATEGORY, msg);
    //}
    //else {
    //  throw new IllegalArgumentException("Incorrect value of LogLevel:" + logLevel);
    //}
  }

  private static String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }

}
