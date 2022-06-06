package com.ringme.CrawlerData.dao.daoImpl;

import com.ringme.CrawlerData.dao.VideoCrawlerDao;
import com.ringme.CrawlerData.entity.Video_crawler_info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class VideoCrawlerDaoImpl implements VideoCrawlerDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Video_crawler_info updateVideoCrawlerInfo(Video_crawler_info video) {
        String sql ="UPDATE video_crawler_info SET active=? WHERE id_video_info=?";
        jdbcTemplate.update(sql, 1 , video.getId());
        return video;
    }

    @Override
    public List<Video_crawler_info> getVideoNotCrawler() {
        String sql="SELECT * FROM video_crawler_info WHERE active=0";
        List<Video_crawler_info> videos=jdbcTemplate.query(sql, new RowMapper<Video_crawler_info>() {
            @Override
            public Video_crawler_info mapRow(ResultSet rs, int rowNum) throws SQLException {
                Video_crawler_info video=new Video_crawler_info();
                video.setId(rs.getInt("id_video_info"));
                video.setType(rs.getString("type"));
                video.setUrl(rs.getString("url"));
                video.setActive(rs.getInt("active"));
                video.setMsisdn(rs.getString("msisdn"));
                video.setCategoryId(rs.getInt("categoryId"));
                return video;
            }
        });
        return videos;
    }
}
