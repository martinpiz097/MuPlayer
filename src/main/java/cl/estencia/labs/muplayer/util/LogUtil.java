package cl.estencia.labs.muplayer.utils;

public class LogUtil {
    public static String getExceptionMsg(Exception exception, String methodName) {
        return "Error on " + methodName + " ("
                + exception.getClass().getSimpleName()
                +"): " + exception.getMessage();
    }
}
