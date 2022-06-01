package com.ringme.CrawlerData.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringme.CrawlerData.dao.VideoCrawlerDao;
import com.ringme.CrawlerData.entity.Video_crawler_info;
import com.ringme.CrawlerData.utils.Validation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class VideoCrawlerServiceImpl {

    @Autowired
    private VideoCrawlerDao videoCrawlerDao;


    private static Logger logger = Logger.getLogger(VideoCrawlerServiceImpl.class);

    private BlockingQueue<Video_crawler_info> queue = new ArrayBlockingQueue<>(10000);

    @Scheduled(fixedDelay = 120000, initialDelay = 1000) // after run 1s - method is called - repeat method 24h
    public void uploadVideoCrawler() {
        List<Video_crawler_info> video_crawler_infos = videoCrawlerDao.getVideoNotCrawler();
        queue.addAll(video_crawler_infos);
        logger.info(video_crawler_infos);
        Video_crawler_info videoCrawlerInfo;
        while ((videoCrawlerInfo=queue.poll()) != null ) {

            if (videoCrawlerInfo.getType().equals("Facebook")) {
                videoCrawlerInfo.setStatus(1); // status downloading
                videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                Video_crawler_info v = crawlerVideoFacebook(videoCrawlerInfo);
                if (v != null) {
                    videoCrawlerInfo.setStatus(2);// status downloaded
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                } else {
                    videoCrawlerInfo.setStatus(3); // status false download
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                }

            } else if (videoCrawlerInfo.getType().equals("Youtube_video")) {
                videoCrawlerInfo.setStatus(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                int v=crawlerSingleVideoYoutube(videoCrawlerInfo);
                if(v==1){
                    videoCrawlerInfo.setStatus(2);
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                }else{
                    videoCrawlerInfo.setStatus(3);
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                }

            } else if (videoCrawlerInfo.getType().equals("Youtube_channel")) {
                videoCrawlerInfo.setStatus(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                int v=crawlerListVideoYoutube(videoCrawlerInfo);
                if(v==1){
                    videoCrawlerInfo.setStatus(2);
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                }else {
                    videoCrawlerInfo.setStatus(3);
                    videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
                }

            } else {
                videoCrawlerInfo.setStatus(3);
                videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
            }
        }
    }


    private Video_crawler_info crawlerVideoFacebook(Video_crawler_info video) {
        logger.info("Link : " + video.getUrl());
        String commandTemplate = "youtube-dl -o /home/anhquan/Video/%(title)s.%(ext)s --sleep-interval 60 %SOURCE_PATH%"; //--sleep-interval 360
        String command = commandTemplate.replace("%SOURCE_PATH%", video.getUrl());
        logger.info("comman : " + command);
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            System.out.println("Comment : " + command);  // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }
            String mediaPath = ""; //Find String contains [download] Destination: /home/anhquan/Video/
            for (String path : lines) {
                if (path.contains("Destination: ")) {
                    mediaPath = path;
                }
            }
            int count1 = mediaPath.indexOf(":"); //set media_path for video
            video.setMedia_path(mediaPath.substring(count1 + 1).trim());
            video.setTitle(video.getTitle() + ".mp4");

            callAPIUpload(video);
        } catch (Exception e) {
            logger.info("FALSE : " + e);
            return null;
        }
        return video;
    }

    private int crawlerListVideoYoutube(Video_crawler_info video) {
        String commandTemplate = "youtube-dl --skip-download --flat-playlist --dump-json --playlist-start 1 --playlist-end 240 %SOURCE_PATH%"; //--sleep-interval 360
        String command = commandTemplate.replace("%SOURCE_PATH%", video.getUrl());
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }
            List<String> idVideo = new ArrayList<>(); // find ID video

            ObjectMapper objectMapper = new ObjectMapper();

            for (String line : lines) {
                JsonNode jsonNode = objectMapper.readTree(line);
                logger.info("jsonNode = " + jsonNode);
                String id = jsonNode.get("id").asText();
                idVideo.add(id);
            }
            logger.info("IdVide|DATA|" + idVideo);

            for (String id : idVideo) {
                String commandTemplate1 = "youtube-dl -o /home/anhquan/Video/%(title)s.%(ext)s --sleep-interval 60 %SOURCE_PATH%"; //--sleep-interval 360
                String command1 = commandTemplate1
                        .replace("%SOURCE_PATH%", "https://www.youtube.com/watch?v=" + id);
                logger.info("link : " + command1);
                Process proc1 = Runtime.getRuntime().exec(command1);
                proc1.waitFor();
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(proc1.getInputStream()));
                List<String> line1s = new ArrayList<>();
                String l;
                while ((l = reader1.readLine()) != null) {
                    line1s.add(l);
                }

                String mediaPath = ""; //Find String contains [download] Destination: /home/anhquan/Video/
                for (String path : line1s) {
                    if (path.contains("Destination: ")) {
                        mediaPath = path;
                    }
                }
                logger.info("Test|mediaPath|DATA|" + mediaPath);
                Video_crawler_info videoCrawler = new Video_crawler_info();
                int count1 = mediaPath.indexOf(":");
                videoCrawler.setMedia_path(mediaPath.substring(count1 + 1).trim());
                int count2 = mediaPath.lastIndexOf("\\");
                logger.info("count 2 : " + count2);
                videoCrawler.setTitle(Validation.validateFileName(mediaPath.substring(count2 + 1).trim()));
                logger.info("video title : " + videoCrawler.getTitle());
                videoCrawler.setMsisdn(video.getMsisdn());
                videoCrawler.setCategoryId(video.getCategoryId());

                int result=callAPIUpload(videoCrawler);
                if(result==0){
                    return 0;
                }
            }
        } catch (Exception e) {
            logger.error("Test|Exception|" + e.getMessage(), e);
            return 0;
        }
        return 1;
    }

    private int crawlerSingleVideoYoutube(Video_crawler_info video) {
        String commandTemplate = "youtube-dl -o D:\\video/%(title)s.%(ext)s --sleep-interval 60 %SOURCE_PATH%"; //--sleep-interval 360
        String command = commandTemplate.replace("%SOURCE_PATH%", video.getUrl());
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }

            String mediaPath = ""; //Find String contains [download] Destination: /home/anhquan/Video/
            for (String path : lines) {
                if (path.contains("Destination: ")) {
                    mediaPath = path;
                }
            }
            logger.info("Test|mediaPath|DATA|" + mediaPath);
            Video_crawler_info videoCrawler = new Video_crawler_info();
            int count1 = mediaPath.indexOf(":");
            videoCrawler.setMedia_path(mediaPath.substring(count1 + 1).trim());
            int count2 = mediaPath.lastIndexOf("\\");
            videoCrawler.setTitle(Validation.validateFileName(mediaPath.substring(count2 + 1).trim()));
            logger.info("video title : " + videoCrawler.getTitle());
            videoCrawler.setMsisdn(video.getMsisdn());
            videoCrawler.setCategoryId(video.getCategoryId());
            int result=callAPIUpload(video);
            if(result==0){
                return 0;
            }
        } catch (Exception e) {
            logger.info("FALSE : " + e);
            return 0;
        }
        return 1;
    }

    private int callAPIUpload(Video_crawler_info videoCrawler) {
        //call API uploadvide at videovcs
        try{
            RestTemplate rest = new RestTemplate();
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
            map.add("msisdn", videoCrawler.getMsisdn());//request parameters
            map.add("mpw", "9EBB7AE993E7FCDFA600E108CC21A259");
            map.add("fName", videoCrawler.getTitle());
            map.add("uFile", new FileSystemResource(videoCrawler.getMedia_path()));
            map.add("timestamp", System.currentTimeMillis());
            map.add("security", "");
            logger.info("DATA:" + map);
            HttpHeaders headers = new HttpHeaders();//request header
            headers.set("mocha-api", "");
            headers.set("Accept-language", "vi");
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
            //Object obj = rest.postForEntity("http://kakoakdev.ringme.vn/video-service/v1/media/video/upload", request, Object.class);
            Object obj = rest.postForEntity("http://freeapi.kakoak.tls.tl/video-service/v1/media/video/upload", request, Object.class);

            //find mediaPath of video after save on server
            String[] strings = obj.toString().split(",");
            int countMediaPath = strings[3].indexOf("=");
            String mediaPathVideoOnServer = strings[3].substring(countMediaPath + 1).trim();

            //call API createVideo at videovcs
            MultiValueMap<String, Object> map1 = new LinkedMultiValueMap<String, Object>();
            map1.add("msisdn", videoCrawler.getMsisdn());//request parameters
            map1.add("timestamp", System.currentTimeMillis());
            map1.add("security", "");
            map1.add("categoryId", videoCrawler.getCategoryId());
            map1.add("videoTitle", videoCrawler.getTitle());
            map1.add("clientType", "clientType");
            map1.add("revision", "revision");
            map1.add("videoDesc", videoCrawler.getTitle());
            map1.add("imageUrl", "/cms_upload/img/2020/12/09/unnamed-1607496877.jpg");
            map1.add("videoUrl", mediaPathVideoOnServer);
            logger.info("DATA:" + map1);
            HttpHeaders headers1 = new HttpHeaders();//request header
            headers1.set("mocha-api", "");
            headers1.set("Accept-language", "en");
            HttpEntity<MultiValueMap<String, Object>> request1 = new HttpEntity<MultiValueMap<String, Object>>(map1, headers);
            Object obj1 = rest.postForEntity("http://kakoakdev.ringme.vn/video-service/v1/user/video/create", request1, Object.class);

            logger.info("Obj = " + obj);
            logger.info("Obj1 = " + obj1);

            //delete video in local after upload success
            File file = new File(videoCrawler.getMedia_path());
            if (file.exists()) {
                System.out.println("Deleted video");
                file.delete();
            } else {
                System.out.println("Video not exist");
            }
        }catch(Exception e){
            logger.info("APIUpload|EXCEPTION : "+ e.getMessage(),e);
            return 0;
        }
        return 1;
    }
}
