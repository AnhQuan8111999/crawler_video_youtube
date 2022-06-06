package com.ringme.CrawlerData.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringme.CrawlerData.dao.VideoCrawlerDao;
import com.ringme.CrawlerData.dao.VideoItemDao;
import com.ringme.CrawlerData.entity.Video_crawler_info;
import com.ringme.CrawlerData.entity.Video_crawler_item;
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

    @Autowired
    private VideoItemDao videoItemDao;


    private static Logger logger = Logger.getLogger(VideoCrawlerServiceImpl.class);

    private BlockingQueue<Video_crawler_item> queue = new ArrayBlockingQueue<>(10000);

    @Scheduled(fixedDelay = 86400, initialDelay = 1000) // after run 1s - method is called - repeat method 24h
    public void getVideoCrawler() {
        List<Video_crawler_info> video_crawler_infos = videoCrawlerDao.getVideoNotCrawler();
        for (Video_crawler_info videoInfo : video_crawler_infos) {

            if (videoInfo.getType().equals("Facebook")) {
                Video_crawler_item videoItem = new Video_crawler_item();
                videoItem.setUrl(videoInfo.getUrl());
                videoItem.setVideoInfo(videoInfo);
                videoItemDao.saveVideoItem(videoItem);
                queue.offer(videoItem);

                videoInfo.setActive(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            } else if (videoInfo.getType().equals("Youtube_video")) {
                Video_crawler_item videoItem = new Video_crawler_item();
                videoItem.setUrl(videoInfo.getUrl());
                videoItem.setVideoInfo(videoInfo);
                videoItemDao.saveVideoItem(videoItem);
                queue.offer(videoItem);

                videoInfo.setActive(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            } else if (videoInfo.getType().equals("Youtube_channel")) {
                List<String> idVideo = getIdVideoChannelYoutube(videoInfo);
                for (String id : idVideo) {
                    String url = "https://www.youtube.com/watch?v=" + id;
                    Video_crawler_item videoItem = new Video_crawler_item();
                    videoItem.setUrl(url);
                    videoItem.setVideoInfo(videoInfo);
                    videoItemDao.saveVideoItem(videoItem);
                    queue.offer(videoItem);
                }
                videoInfo.setActive(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            } else {
                videoInfo.setActive(2);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            }
        }
    }

    private List<String> getIdVideoChannelYoutube(Video_crawler_info videoInfo) {
        String commandTemplate = "youtube-dl --skip-download --flat-playlist --dump-json --playlist-start 1 --playlist-end 240 %SOURCE_PATH%"; //--sleep-interval 360
        String command = commandTemplate.replace("%SOURCE_PATH%", videoInfo.getUrl());
        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }
            reader.close();
            List<String> idVideo = new ArrayList<>(); // find ID video
            ObjectMapper objectMapper = new ObjectMapper();
            for (String line : lines) {
                JsonNode jsonNode = objectMapper.readTree(line);
                String id = jsonNode.get("id").asText();
                idVideo.add(id);
            }
            logger.info("IdVide|SIZE : " + idVideo.size() + " - IdVideo|Data : " + idVideo);
            return idVideo;
        } catch (Exception e) {
            logger.info("GetIdVideoChannelYoutobe|Exception : " + e.getMessage(), e);
            return null;
        }
    }

    @Scheduled(fixedDelay = 86400, initialDelay = 1000)
    private void crawlerAndUploadVideo() {
        while((Video_crawler_item videoItem=queue.poll()) != null){
        List<String> commands = new ArrayList<String>();
        commands.add("youtube-dl");
        commands.add("-o");
        commands.add("~/VideoCrawler/%(title)s.%(ext)s");
        commands.add("--sleep-interval");
        commands.add("3");
        commands.add(videoItem.getUrl());
        commands.add("-f");
        commands.add("mp4");
        logger.info("Link|Download : " + commands.toString());
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);

        try {
            Process proc = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String l;
            while ((l = reader.readLine()) != null) {
                lines.add(l);
            }
            proc.waitFor();
            reader.close();

            String mediaPath = ""; //Find String contains [download] Destination: /home/anhquan/Video/
            for (String path : lines) {
                if (path.contains("Destination: ")) {
                    mediaPath = path;
                }
            }
            logger.info("MediaPath|DATA : " + mediaPath);
            Video_crawler_info videoCrawler = new Video_crawler_info();
            int count1 = mediaPath.indexOf(":");
            videoCrawler.setMedia_path(mediaPath.substring(count1 + 1).trim());
            logger.info("video|path : " + videoCrawler.getMedia_path());
            int count2 = mediaPath.lastIndexOf("/");
            videoCrawler.setTitle(Validation.validateFileName(mediaPath.substring(count2 + 1).trim()));
            logger.info("video title : " + videoCrawler.getTitle());
            videoCrawler.setMsisdn(videoItem.getVideoInfo().getMsisdn());
            videoCrawler.setCategoryId(videoItem.getVideoInfo().getCategoryId());

//            Video_crawler_item videoItem = new Video_crawler_item();
//            videoItem.setUrl(videoItem.getUrl());
//            videoItem.setTitle(videoCrawler.getTitle());
//            videoItem.setVideoInfo(videoItem);
//            videoItemDao.saveVideoItem(videoItem);

            int result = callAPIUpload(videoCrawler);
            if (result == 0) {
            }
        } catch (Exception e) {
            logger.error("Download|Exception : " + e.getMessage(), e);
        }
    }

    private int callAPIUpload(Video_crawler_item videoItem) {
        //call API uploadvide at videovcs
        try {
            RestTemplate rest = new RestTemplate();
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
            map.add("msisdn", videoItem.getVideoInfo().getMsisdn());//request parameters
            map.add("mpw", "9EBB7AE993E7FCDFA600E108CC21A259");
            map.add("fName", videoItem.getTitle());
            map.add("uFile", new FileSystemResource(videoItem.getMedia_path()));
            map.add("timestamp", System.currentTimeMillis());
            map.add("security", "");
            logger.info("DATA:" + map);
            HttpHeaders headers = new HttpHeaders();//request header
            headers.set("mocha-api", "");
            headers.set("Accept-language", "vi");
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
            Object obj = rest.postForEntity("http://freeapi.kakoak.tls.tl/video-service/v1/media/video/upload", request, Object.class);

            //find mediaPath of video after save on server
            String[] strings = obj.toString().split(",");
            int countMediaPath = strings[3].indexOf("=");
            String mediaPathVideoOnServer = strings[3].substring(countMediaPath + 1).trim();

            //call API createVideo at videovcs
            MultiValueMap<String, Object> map1 = new LinkedMultiValueMap<String, Object>();
            map1.add("msisdn", videoItem.getVideoInfo().getMsisdn());//request parameters
            map1.add("timestamp", System.currentTimeMillis());
            map1.add("security", "");
            map1.add("categoryId", videoItem.getVideoInfo().getCategoryId());
            map1.add("videoTitle", videoItem.getTitle());
            map1.add("clientType", "clientType");
            map1.add("revision", "revision");
            map1.add("videoDesc", videoItem.getTitle());
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
            File file = new File(videoItem.getMedia_path());
            if (file.exists()) {
                System.out.println("Deleted video");
                file.delete();
            } else {
                System.out.println("Video not exist");
            }
        } catch (Exception e) {
            logger.info("APIUpload|EXCEPTION : " + e.getMessage(), e);
            return 0;
        }
        return 1;
    }
}


//    private Video_crawler_info crawlerVideoFacebook(Video_crawler_info video) {
//        logger.info("Link : " + video.getUrl());
//        String commandTemplate = "youtube-dl -o ~/VideoCrawler/%(title)s.%(ext)s --sleep-interval 300 %SOURCE_PATH% -f mp4"; //--sleep-interval 360
//        String command = commandTemplate.replace("%SOURCE_PATH%", video.getUrl());
//        logger.info("comman : " + command);
//        try {
//            Process proc = Runtime.getRuntime().exec(command);
//            proc.waitFor();
//            System.out.println("Comment : " + command);  // Read the output
//            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            List<String> lines = new ArrayList<>();
//            String s;
//            while ((s = reader.readLine()) != null) {
//                lines.add(s);
//            }
//            String mediaPath = ""; //Find String contains [download] Destination: /home/anhquan/Video/
//            for (String path : lines) {
//                if (path.contains("Destination: ")) {
//                    mediaPath = path;
//                }
//            }
//            int count1 = mediaPath.indexOf(":"); //set media_path for video
//            video.setMedia_path(mediaPath.substring(count1 + 1).trim());
//            video.setTitle(video.getTitle() + ".mp4");
//
//            Video_crawler_item videoItem = new Video_crawler_item();
//            videoItem.setUrl(video.getUrl());
//            videoItem.setTitle(video.getTitle());
//            videoItem.setVideoInfo(video);
//            videoItemDao.saveVideoItem(videoItem);
//
//            callAPIUpload(video);
//        } catch (Exception e) {
//            logger.info("Download|Exception| " + e.getMessage(), e);
//            return null;
//        }
//        return video;
//    }