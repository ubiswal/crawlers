package com.ubiswal.utils;

import java.util.ArrayList;
import java.util.List;

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
}
