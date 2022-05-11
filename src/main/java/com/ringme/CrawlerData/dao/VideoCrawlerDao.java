package com.ringme.CrawlerData.dao;

import com.ringme.CrawlerData.entity.Video_crawler_info;

import java.util.List;

public interface VideoCrawlerDao {

    Video_crawler_info updateVideoCrawlerInfo(Video_crawler_info videoCrawlerInfo);

    List<Video_crawler_info> getVideoNotCrawler ();


}
