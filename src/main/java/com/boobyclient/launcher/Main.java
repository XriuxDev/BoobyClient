package com.boobyclient.launcher;

/**
 * Entry point wrapper to bypass JavaFX runtime issues on non-modular classpath
 */
public class Main {
    public static void main(String[] args) {
        // This class does NOT extend Application, so the JVM won't check for 
        // JavaFX components before starting the main method.
        ModernLauncherApp.main(args);
    }
}
