package com.ringme.CrawlerData.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video_crawler_item {
    private int id;

    private String url;

    private String title;

    private String download_time =formatDate();

    private Video_crawler_info videoInfo;

    private String formatDate(){
        LocalDateTime localTime = LocalDateTime.now();
        DateTimeFormatter FormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String time = localTime.format(FormatObj);
        return time;
    }
}
