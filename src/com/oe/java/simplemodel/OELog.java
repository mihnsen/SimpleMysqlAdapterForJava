package com.oe.java.simplemodel;

/**
 *
 * @author minhnt
 */
public class OELog {
    
    
    public static void d(String msg) {
        System.out.println("LOG: "+msg);
    }
    
    public static void e(String msg) {
        System.err.println("ERROR: "+msg);
    }
}
