import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String commandTemplate = "youtube-dl -o ~/VideoCrawler/%(title)s.%(ext)s  https://www.youtube.com/watch?v=D1d7-6z2-0k";
        System.out.println(commandTemplate);
        try {
           Process proc1 = Runtime.getRuntime().exec(commandTemplate);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc1.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }
            proc1.waitFor();
            reader.close();
            System.out.println("Line : "+ lines.toArray());
        } catch (Exception e) {
            System.out.println("False : " + e);
        }
    }
}