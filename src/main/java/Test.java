import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String commandTemplate = "youtube-dl --skip-download --flat-playlist --dump-json --playlist-start 1 --playlist-end 5  https://www.youtube.com/c/th%C3%A2ntri%E1%BB%87u/videos";


        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(commandTemplate);
            proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }

            List<String> videoInfo = new ArrayList<>() ;
            for(String line:lines){
                String lineFor[]=line.split(",");
                String s1=lineFor[3].substring(9,lineFor[3].length()-1);
                videoInfo.add(s1);
            }
            for(String test:videoInfo){
                System.out.println(test);
            }
        } catch (Exception e) {
            System.out.println("False : " + e);
        }
    }
}