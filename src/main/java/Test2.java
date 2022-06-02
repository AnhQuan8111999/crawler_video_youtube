import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test2 {
    public static void main(String[] args) {
        String commandTemplate = "youtube-dl"+ "-o"+ "~/VideoCrawler/%(title)s.%(ext)s"+  "https://www.youtube.com/watch?v=D1d7-6z2-0k";
        List<String> commands = new ArrayList<String>();
        commands.add("youtube-dl");
        commands.add("-o");
        commands.add("~/VideoCrawler/%(title)s.%(ext)s");
        commands.add("--sleep-interval");
        commands.add("3");
        commands.add("https://www.youtube.com/watch?v=urZ0bhF9bB4"); // https://www.youtube.com/watch?v=D1d7-6z2-0k
        System.out.println(commands);
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        try {
            Process proc1 = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc1.getInputStream()));
            List<String> lines = new ArrayList<>();
            String s;
            while ((s = reader.readLine()) != null) {
                lines.add(s);
            }
            proc1.waitFor();
            reader.close();

            String mediaPath = "";
            ObjectMapper objectMapper = new ObjectMapper();
            for(String line : lines){
                JsonNode jsonNode = objectMapper.readTree(line);
                System.out.println("jsonNode = " + jsonNode);
                 mediaPath = jsonNode.get("Destination").asText();
            }
            System.out.println("Media|Path : " + mediaPath);
            System.out.println("Line : "+ lines.toString());
        } catch (Exception e) {
            System.out.println("False : " + e);
        }
    }
}
