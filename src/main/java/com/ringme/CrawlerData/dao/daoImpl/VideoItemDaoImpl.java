package com.ringme.CrawlerData.dao.daoImpl;

import com.ringme.CrawlerData.dao.VideoItemDao;
import com.ringme.CrawlerData.entity.Video_crawler_info;
import com.ringme.CrawlerData.entity.Video_crawler_item;
import com.ringme.CrawlerData.service.serviceImpl.VideoCrawlerServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VideoItemDaoImpl implements VideoItemDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(VideoItemDaoImpl.class);

    @Override
    public Video_crawler_item saveVideoItem(Video_crawler_item videoItem) {
        try {
            String sql = "INSERT INTO video_crawler_item (url_video_item,video_crawler_info_id,status) VALUE (?,?,?)";
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
            String sql ="UPDATE video_crawler_item SET title=?,download_time=?, status=? WHERE id_video_item like ?";
            jdbcTemplate.update(sql,videoItem.getTitle(),videoItem.getDownload_time() ,videoItem.getStatus() , videoItem.getId());
            return 1;
        }catch(Exception e){
            logger.info("UpdateVideoItem|Exception : "+ e.getMessage(),e);
            return 0;
        }

    }

    @Override
    public List<Video_crawler_item> getVideoItems() {
        List<Video_crawler_item> videoItems=new ArrayList<>();
        String sql="SELECT * FROM video_crawler_item item\n" +
                "INNER JOIN kakoak.video_crawler_info info " +
                "WHERE item.video_crawler_info_id =info.id_video_info AND item.status in (0,1)";
        videoItems=jdbcTemplate.query(sql, new RowMapper<Video_crawler_item>() {
            @Override
            public Video_crawler_item mapRow(ResultSet rs, int rowNum) throws SQLException {
                Video_crawler_info videoInfo=new Video_crawler_info();
                videoInfo.setMsisdn(rs.getString("msisdn"));
                videoInfo.setCategoryId(rs.getInt("categoryId"));
                videoInfo.setId(rs.getInt("id_video_info"));
                videoInfo.setType(rs.getString("type"));
                videoInfo.setUrl(rs.getString("url"));

                Video_crawler_item videoItem=new Video_crawler_item();
                videoItem.setUrl(rs.getString("url_video_item"));
                videoItem.setStatus(rs.getInt("status"));
                videoItem.setId(rs.getInt("id_video_item"));
                videoItem.setVideoInfo(videoInfo);
                return videoItem;
            }
        });
        return videoItems;
    }
}
