package com.syber.base.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by liangtg on 16-11-16.
 */

public class IOUtils {
    private IOUtils() {
    }

    public static void closeSilenty(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }
}
