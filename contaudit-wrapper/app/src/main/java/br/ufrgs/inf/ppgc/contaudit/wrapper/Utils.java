package br.ufrgs.inf.ppgc.contaudit.wrapper;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Utils {
    private Utils() {}
    
    public static boolean isDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    }

    public static String getCurrentDirectory() {
        try {
            return new File(Startup.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
        
    public static SimpleDateFormat dateFormatter() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
    }

    public static String transformInputStreamToString(InputStream output, boolean showDate) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(output, StandardCharsets.UTF_8);
        while (scanner.hasNextLine()) {
            sb.append(formatLog(scanner.nextLine(), showDate));
        }
        scanner.close();
        
        return sb.toString();
    }

    private static String formatLog(String message, boolean showDate) {
        String log = "";
        if (showDate) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
            log = dateFormat.format(new Date()) + ": " + message + System.getProperty("line.separator");
        } else {
            log = message + System.getProperty("line.separator");
        }

        return log;
    }
}
