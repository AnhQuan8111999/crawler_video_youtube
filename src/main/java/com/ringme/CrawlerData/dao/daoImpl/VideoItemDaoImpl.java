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
    public Video_crawler_item saveVideoItem(Video_crawler_item videoItem) {
        try {
            String sql = "INSERT INTO video_crawler_item (url,video_crawler_info_id,status) VALUE (?,?,?)";
            jdbcTemplate.update(sql, videoItem.getUrl(),videoItem.getVideoInfo().getId(),0);
            return videoItem;
        }catch (Exception e){
            logger.info("Save|InfoVideo|Exception : "+e.getMessage() ,e);
            return null;
        }
    }

    @Override
    public int updateVideoItem(Video_crawler_item videoItem) {
        try{
            String sql ="UPDATE video_crawler_info SET title=?, status=? WHERE id=?";
            jdbcTemplate.update(sql,videoItem.getTitle(), videoItem.getStatus() , videoItem.getId());
            return 1;
        }catch(Exception e){
            logger.info("UpdateVideoItem|Exception : "+ e.getMessage(),e);
            return 0;
        }

    }
}
