package edu.mcw.rgd.variantIndexer;

public class Utils {
    public static String formatElapsedTime(long t1, long t2) {
        long diff = t2 - t1;
        if (diff < 0L) {
            diff = -diff;
        }

        long secondInMillis = 1000L;
        long minuteInMillis = secondInMillis * 60L;
        long hourInMillis = minuteInMillis * 60L;
        long dayInMillis = hourInMillis * 24L;
        long yearInMillis = dayInMillis * 365L;
        long elapsedYears = diff / yearInMillis;
        diff %= yearInMillis;
        long elapsedDays = diff / dayInMillis;
        diff %= dayInMillis;
        long elapsedHours = diff / hourInMillis;
        diff %= hourInMillis;
        long elapsedMinutes = diff / minuteInMillis;
        diff %= minuteInMillis;
        long elapsedSeconds = diff / secondInMillis;
        StringBuilder buf = new StringBuilder(100);
        if (elapsedYears > 0L) {
            buf.append(elapsedYears).append(" years ");
        }

        if (elapsedDays > 0L) {
            buf.append(elapsedDays).append(" days ");
        }

        if (elapsedHours > 0L) {
            buf.append(elapsedHours).append(" hours ");
        }

        if (elapsedMinutes > 0L) {
            buf.append(elapsedMinutes).append(" minutes ");
        }

        if (elapsedSeconds > 0L) {
            buf.append(elapsedSeconds).append(" seconds ");
        }

        String elapsedTime = buf.toString();
        return elapsedTime.length() > 0 ? elapsedTime : "0";
    }
}
