package it.nexera.ris.common.helpers;

public class EnumHelper {
    public static <E extends Enum<E>> String toStringFormatter(E obj) {
        if (obj == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName().substring(0, 1).toLowerCase());
        sb.append(obj.getClass().getSimpleName().substring(1));
        sb.append(obj.name());

        return sb.toString();
    }

    public static <E extends Enum<E>> String toStringFormatterShort(E obj) {
        if (obj == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName().substring(0, 1).toLowerCase());
        sb.append(obj.getClass().getSimpleName().substring(1));
        sb.append(obj.name());
        sb.append("ShortValue");

        return sb.toString();
    }

}
