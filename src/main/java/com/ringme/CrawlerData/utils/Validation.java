package com.ringme.CrawlerData.utils;


public class Validation {

    public static String validateFileName(String name) {
        String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]"; // xoa ki tu dac biet de upload
        return name.replaceAll(characterFilter ,"");
    }

//    public static void main(String[] args) {
//        String s="의대생 4학년이랑 3시간 빡공하실래요? \uD83D\uDD25 MEDICAL STUDENT 3HR STUDY WITH ME (real time, real sound)";
//        System.out.println(s);
//        s=s.replaceAll("[^a-zA-Z0-9]", " ");
//        System.out.println(s);
//    }
}
