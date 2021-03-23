package study.acodexm.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WrapType {
    public static String[] items = new String[]{"Spherical", "Cylindrical", "Plane", "Fisheye", "Stereographic"};

    public static String get(int position) {
        return items[position].toLowerCase();
    }

    public static int getPosition(String name) {
        return Arrays.stream(items).map(String::toUpperCase).collect(Collectors.toList()).indexOf(name.toUpperCase());
    }
}
