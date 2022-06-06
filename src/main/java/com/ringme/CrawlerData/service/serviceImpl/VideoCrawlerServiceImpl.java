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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Scheduled(fixedDelay = 86400000, initialDelay = 1000) // after run 1s - method is called - repeat method 24h
    public void getVideoCrawler() {
        List<Video_crawler_info> video_crawler_infos = videoCrawlerDao.getVideoNotCrawler();
        for (Video_crawler_info videoInfo : video_crawler_infos) {

            if (videoInfo.getType().equals("Facebook")) {
                Video_crawler_item videoItem = new Video_crawler_item();
                videoItem.setUrl(videoInfo.getUrl());
                videoItem.setVideoInfo(videoInfo);
                videoItem.setVideoInfo(videoInfo);
                videoItemDao.saveVideoItem(videoItem);

                videoInfo.setActive(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            } else if (videoInfo.getType().equals("Youtube_video")) {
                Video_crawler_item videoItem = new Video_crawler_item();
                videoItem.setUrl(videoInfo.getUrl());
                videoItem.setVideoInfo(videoInfo);
                videoItemDao.saveVideoItem(videoItem);

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
                }
                videoInfo.setActive(1);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            } else {
                videoInfo.setActive(2);
                videoCrawlerDao.updateVideoCrawlerInfo(videoInfo);
            }
        }
        List<Video_crawler_item> videoItems=new ArrayList<>();
        videoItems=videoItemDao.getVideoItems();
        queue.addAll(videoItems);
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

    @Scheduled(fixedDelay = 43200000, initialDelay = 10000) // method is called after 12h
    private void crawlerAndUploadVideo() {
//        logger.info("Queue : "+ queue.toString());
        logger.info("Queue.size  : "+ queue.size());
        Video_crawler_item videoItem=new Video_crawler_item();
        while(( videoItem=queue.poll()) != null) {
            videoItem.setStatus(1);
            videoItemDao.updateVideoItem(videoItem);

            List<String> commands = new ArrayList<String>();
            commands.add("youtube-dl");
            commands.add("-o");
            commands.add("~/VideoCrawler/%(title)s.%(ext)s");
            commands.add("--sleep-interval");
            commands.add("300");
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
                int count1 = mediaPath.indexOf(":");
                videoItem.setMedia_path(mediaPath.substring(count1 + 1).trim());
                logger.info("video|path : " + videoItem.getMedia_path());
                int count2 = mediaPath.lastIndexOf("/");
                videoItem.setTitle(Validation.validateFileName(mediaPath.substring(count2 + 1).trim()));
                logger.info("video title : " + videoItem.getTitle());

                callAPIUpload(videoItem);

                LocalDateTime localTime = LocalDateTime.now();
                DateTimeFormatter FormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String time = localTime.format(FormatObj);
                videoItem.setDownload_time(time);
                videoItem.setStatus(2);
                videoItemDao.updateVideoItem(videoItem);
            } catch (Exception e) {
                logger.info("Download|Exception : " + e.getMessage(), e);
                videoItem.setStatus(3);
                videoItemDao.updateVideoItem(videoItem);
                continue;
            }
        }
    }

    private int callAPIUpload(Video_crawler_item videoItem) throws Exception{
            //call API uploadvide at videovcs
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
        return 1;
    }
}
