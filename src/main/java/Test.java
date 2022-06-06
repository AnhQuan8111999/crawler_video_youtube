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