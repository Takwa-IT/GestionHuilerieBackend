package Config;

public final class ReferenceUtils {

    private ReferenceUtils() {
    }

    public static String format(String prefix, Long id) {
        if (id == null) {
            return null;
        }
        return prefix + String.format("%02d", id);
    }
}
