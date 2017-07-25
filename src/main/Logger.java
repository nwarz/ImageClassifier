package main;

import main.gui.KNNClassifierView;

public class Logger {

    private enum LoggerType {
        CONSOLE,
        SWING_GUI
    }


    private LoggerType loggerType = LoggerType.CONSOLE;
    private KNNClassifierView knnClassifierView;

    private void logToConsole(String message) {
        System.out.println(message);
    }

    private void logToGUIStatusField(String message) {
        if(theLogger.knnClassifierView == null) {
            throw new NullPointerException("Logger.knnClassifierView not initialized");
        }
        knnClassifierView.writeToStatusArea(message);
    }


    private static Logger theLogger = new Logger();

    public static void log(String message) {
        if(theLogger.loggerType == LoggerType.CONSOLE) {
            theLogger.logToConsole(message);
        } else {
            assert theLogger.loggerType == LoggerType.SWING_GUI;
            theLogger.logToGUIStatusField(message);
        }
    }

    /**
     * Sets the logger to output to the Swing GUI text console
     * @param textArea text console to writeToStatusArea to
     */
    public static void setToSwingLogger(KNNClassifierView textArea) {
        theLogger.loggerType = LoggerType.SWING_GUI;
        theLogger.knnClassifierView = textArea;
    }

    /**
     * Sets the logger to output to the default console
     */
    public static void setToConsoleLogger() {
        theLogger.loggerType = LoggerType.CONSOLE;
        theLogger.knnClassifierView = null;
    }
}
