package com.ringme.CrawlerData.dao.daoImpl;

import com.ringme.CrawlerData.dao.VideoItemDao;
import com.ringme.CrawlerData.entity.Video_crawler_item;
import com.ringme.CrawlerData.service.serviceImpl.VideoCrawlerServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VideoItemDaoImpl implements VideoItemDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(VideoItemDaoImpl.class);

    @Override
    public Video_crawler_item saveVideoItem(Video_crawler_item video_crawler_item) {
        try {
            String sql = "INSERT INTO video_crawler_item (url,title,download_time,video_crawler_info_id) VALUE (?,?,?,?)";
            jdbcTemplate.update(sql, video_crawler_item.getUrl(), video_crawler_item.getTitle(),
                    video_crawler_item.getDownload_time(), video_crawler_item.getVideoInfo().getId());
            return video_crawler_item;
        }catch (Exception e){
            logger.info("Save|InfoVideo|Exception : "+e.getMessage() ,e);
            return null;
        }
    }
}
