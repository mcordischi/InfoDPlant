package com.infodplant;

import org.opencv.core.Point;

import java.io.OutputStream;
import java.util.List;

/**
 * Created by marto on 11/5/13.
 */
public class Utils {
    public static char encode64(int i)
    {
        if( i == 0x00) return 0x2D;
        if( i <  0x0B) return (char)(i+0x2F);
        if( i <  0x25) return (char)(i+0x36);
        if( i == 0x25) return 0x5F;
        return (char)(i+0x3B);
    }

    public static int decode64(char c)
    {
        if( c == 0x2D) return 0;
        if( c <  0x3A) return (c-0x2F);
        if( c <  0x5B) return (c-0x36);
        if( c == 0x5F) return 0x25;
        return (c-0x3B);
    }
}
