import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        int[] array={1,2,3};
        float a=5;
        for(int i=0;i< array.length;i++){
            try {
                if(i==0){
                    System.out.println("array : "+ array[10]);
                }else{
                    System.out.println("array["+i+"] = "+array[i]);
                }
            }catch(Exception e){
                System.out.println("Exception : "+ e.getMessage());
                continue;
            }
        }
    }
}

//videoCrawlerInfo.setStatus(1); // status downloading
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        Video_crawler_info v = crawlerVideoFacebook(videoCrawlerInfo);
//        if (v != null) {
//        videoCrawlerInfo.setStatus(2);// status downloaded
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        } else {
//        videoCrawlerInfo.setStatus(3); // status false download
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        }
//
//        videoCrawlerInfo.setStatus(1);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        int v = crawlerSingleVideoYoutube(videoCrawlerInfo);
//        if (v == 1) {
//        videoCrawlerInfo.setStatus(2);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        } else {
//        videoCrawlerInfo.setStatus(3);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        }
//
//        videoCrawlerInfo.setStatus(1);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        int v = crawlerListVideoYoutube(videoCrawlerInfo);
//        if (v == 1) {
//        videoCrawlerInfo.setStatus(2);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        } else {
//        videoCrawlerInfo.setStatus(3);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);
//        }
//
//        videoCrawlerInfo.setStatus(3);
//        videoCrawlerDao.updateVideoCrawlerInfo(videoCrawlerInfo);