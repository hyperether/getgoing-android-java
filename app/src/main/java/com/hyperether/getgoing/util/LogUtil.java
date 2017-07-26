package com.hyperether.getgoing.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.BuildConfig;
import com.hyperether.getgoing.manager.FileManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for persisting logs into app folder
 *
 * @author prijic
 * @version 1.0 11/03/2015
 */
public class LogUtil {

    /*
        try/catch block error caught
     */
    public static final int ERROR = 1;
    /*
        App flow important logs
     */
    public static final int DEBUG = 2;
    /*
        QA logs; will be masked for production
     */
    public static final int VERBOSE = 3;
    /*
     Info
     */
    public static final int INFO = 4;


    private static LogUtil instance;
    private String logFilePath;

    public LogUtil() {
        logFilePath = FileManager.getFileStoragePath(
                GetGoingApp.getInstance().getApplicationContext()) + "/log.html";
    }

    public static LogUtil getInstance() {
        if (instance == null)
            instance = new LogUtil();
        return instance;
    }

    public void add(int level, String tag, String text, Exception ex) {

        String log = "";

        if (text != null) {
            if (ex == null)
                log = text;
            else if (ex.getMessage() != null)
                log = text + "->" + ex.getMessage();
            else if (ex.toString() != null)
                log = text + "->" + ex.toString();
        }

        switch (level) {
            case ERROR:
                Log.e(tag, log);
                break;
            case DEBUG:
                Log.d(tag, log);
                break;
            case VERBOSE:
                Log.v(tag, log);
                break;
            case INFO:
                Log.i(tag, log);
                break;
        }

        if (BuildConfig.FILE_LOG_ACTIVE)
            addLogToFile(level, tag, log);
    }

    public void addOutOfMemory(int level, String tag, String text, OutOfMemoryError ex) {

        String log = "";

        if (text != null) {
            if (ex == null)
                log = text;
            else if (ex.getMessage() != null)
                log = text + "->" + ex.getMessage();
            else if (ex.toString() != null)
                log = text + "->" + ex.toString();
        }

        switch (level) {
            case ERROR:
                Log.e(tag, log);
                break;
            case DEBUG:
                Log.d(tag, log);
                break;
            case VERBOSE:
                Log.v(tag, log);
                break;
            case INFO:
                Log.i(tag, log);
        }

        if (BuildConfig.FILE_LOG_ACTIVE)
            addLogToFile(level, tag, log);
    }

    public void sendLogFile(Activity activity) {
        if (logFilePath != null) {
            List<String> filePaths = new ArrayList<>();
            filePaths.add(logFilePath);

            //need to "send multiple" to get more than one attachment
            final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("text/plain");
//        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
//        emailIntent.putExtra(android.content.Intent.EXTRA_CC, new String[]{emailCC});
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
            //has to be an ArrayList
            ArrayList<Uri> uris = new ArrayList<>();
            //convert from paths to Android friendly Parcelable Uri's
            for (String file : filePaths) {
                File fileIn = new File(file);
                Uri u = Uri.fromFile(fileIn);
                uris.add(u);
            }
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }
    }

    private void addLogToFile(int level, String tag, String log) {
        File logFile = null;
        String logTag = "";
        String sLevel;
        switch (level) {
            case ERROR:
                sLevel = "ERROR";
                break;
            case DEBUG:
                sLevel = "DEBUG";
                break;
            case VERBOSE:
                sLevel = "VERBOSE";
                break;
            case INFO:
                sLevel = "INFO";
                break;
            default:
                sLevel = "WARN";
        }
        Date time = new Date();

        logTag =
                "<style>\n table, th, td { border: 1px solid black;} </style>\n" +
                        "<table>" +
                        "<tr style=\"" + getLevelColor(level) + "\">\n" +
                        "<td id=\"" + time + "\">" + new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(time) + "</td>\n" +
                        "<td title=\"Level\">" + sLevel + "</font></td>\n" +
                        "<td title=\"TAG\">" + tag + "</td>\n" +
                        "<td title=\"Message\">" + log + "</td>\n" +
                        "</tr>" +
                        "</table>";

        logFile = new File(logFilePath);
        boolean fileExist = logFile.exists();

        if (fileExist) {
            if (logFile.length() > 5000000) {
                logFile.delete();
                fileExist = false;
            }
        }

        if (!fileExist) {
            try {
                fileExist = logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fileExist) {
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(logTag);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the background color for a particular event
     *
     * @param level log level
     */
    private String getLevelColor(int level) {
        switch (level) {
            case ERROR:
                return "color:rgb(255,0,0);";
            case DEBUG:
                return "color:rgb(255,153,0);";
            case VERBOSE:
                return "color:rgb(0, 255, 0)";
            case INFO:
                return "color:rgb(0, 51, 204)";
            default:
                return "color:rgb(204, 255, 255)";
        }
    }
}