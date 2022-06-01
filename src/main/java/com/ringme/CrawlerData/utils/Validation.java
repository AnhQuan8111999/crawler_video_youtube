package com.ringme.CrawlerData.utils;


public class Validation {

    public static String validateFileName(String name) {
        String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]"; // xoa ki tu dac biet de upload
        return name.replaceAll(characterFilter ,"");
    }

}
