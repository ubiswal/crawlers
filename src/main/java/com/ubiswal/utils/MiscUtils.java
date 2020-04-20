package com.ubiswal.utils;

import java.text.SimpleDateFormat;
import java.util.*;

public class MiscUtils {
    public static List<List<String>> partition(List<String> fullList, int batchSize) {
        List retval = new ArrayList();
        int i;
        for (i = 0; i+batchSize < fullList.size(); i = i+batchSize){
            retval.add(fullList.subList(i, i+batchSize));
        }
        if (i < fullList.size()-1){
            retval.add(fullList.subList(i, fullList.size()));
        }
        return retval;
    }

    public static String getS3FolderPath(String symbol, String fileName) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        String rootFolderName = formatter.format(date);
        return String.format("%s/%s/%s/%s", rootFolderName, hour, symbol, fileName);
    }
}
