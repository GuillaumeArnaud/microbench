package fr.xebia.microbench.internals

import groovy.transform.CompileStatic

import static java.lang.Math.round

@CompileStatic
class Utils {

    public static String prettyBytes(Long bytes) {
        if (bytes > 1024 * 1024 * 1024) "${round((float) bytes / (1024 * 1024 * 1024))}GB"
        else if (bytes > 1024 * 1024) "${round((float) bytes / (1024 * 1024))}MB"
        else if (bytes > 1024) "${round((float) bytes / (1024))}KB"
        else "${round((float) bytes / (1024))}B"
    }

    public static String unit(float timeInNs) {
        if (timeInNs < 1000000f) {
            return "$timeInNs ns"
        } else return "${timeInNs / 1000000} ms"
    }
}
