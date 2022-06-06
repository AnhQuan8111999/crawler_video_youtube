package com.ringme.CrawlerData.dao;

import com.ringme.CrawlerData.entity.Video_crawler_item;

import java.util.List;

public interface VideoItemDao {
    public Video_crawler_item saveVideoItem (Video_crawler_item video_crawler_item);

    public int updateVideoItem(Video_crawler_item videoInfo);

    public List<Video_crawler_item> getVideoItems();
}
