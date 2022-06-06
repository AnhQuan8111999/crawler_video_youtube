package com.ringme.CrawlerData.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video_crawler_info {

    private int id;

    private String type;

    private String url;

    private int active;

    private String msisdn;

    private int categoryId;

}
