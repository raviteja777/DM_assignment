
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by raviteja on 16-04-2017.
 * io manager reads tbe input lines from file
 * writes output along with missing lines in another file
 *
 */

public class IOManager {

    private static final TreeMap<LocalTime, List<String>> timeMsgMap = new TreeMap<>();
    private static final String FILL_MSG = "missing text here";
    private static final String PREFIX = "Mon Feb 29";

    public static void main(String args[]){
        //file path relative  -- change it accordingly
        final File input_file= new File("proj_files\\input_file");
        final File output_file= new File("proj_files\\output_file");
        //buffer file for sending to regression
        Map<Integer,Integer> buffer = new TreeMap<>();
        //read data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(input_file))){
            String line ;
            while((line=reader.readLine())!=null) {
                addToMap(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //traverse thru map and find gaps
        LocalTime end = timeMsgMap.lastKey().minusMinutes(2);
        LocalTime prev = timeMsgMap.firstKey();
        LocalTime curr = timeMsgMap.higherKey(prev);
        System.out.println("start :"+prev+" ,end: "+end);
        while(curr.compareTo(end)<=0){
            if(curr.minusMinutes(1).equals(prev)){
                prev = curr;
                curr = timeMsgMap.higherKey(prev);
            }
            else{
                LocalTime missing = prev.plusMinutes(1);
                writeBuffer(buffer,timeMsgMap.lowerKey(prev),prev,curr,timeMsgMap.higherKey(curr));
                TimeStampAdjuster tsadj = new TimeStampAdjuster(buffer,timeToInt(missing));
                int linesPerMin = tsadj.updateMissing();
                timeMsgMap.put(missing,genStatement(missing,linesPerMin));
                System.out.println("test data result : "+missing+"-->"+linesPerMin+ "\n -----------------");
                prev = missing;
                buffer.clear();
            }
        }
        //output to console
        //timeMsgMap.forEach((x,y)->System.out.println(x+"-->"+y));

        //write output to file -- try with resources
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(output_file))){
            timeMsgMap.forEach((x,y)-> y.forEach(s -> {
                try {
                    writer.write(s);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //inserts records from input line to TreeMap
        public static void addToMap(String line){
            //find pattern for time in HH:mm:ss format
            Pattern pat = Pattern.compile("\\s+\\d{2}:\\d{2}:\\d{2}\\s+");
            Matcher mat = pat.matcher(line);
            if(mat.find()){
                String time_string = mat.group().trim();
                LocalTime time = LocalTime.parse(time_string);
                time = time.minusSeconds(time.getSecond());
                List<String> msgList = timeMsgMap.get(time);
                if(msgList==null){
                    msgList = new ArrayList<>();
                    timeMsgMap.put(time,msgList);
                }
                msgList.add(line);
            }
        }
        //writes t-2,t-1 , t+1 , t+2 records in a buffer file -- before regression step
        public static void writeBuffer(Map<Integer,Integer> buffer,LocalTime... timeArr){
            //key1 = timeMsgMap.lowerKey(key2) , key2 = prev , key3 =curr , key4 = timeMsgMap.higherKey(key3)
            for(LocalTime key : timeArr){
                buffer.put(timeToInt(key),timeMsgMap.get(key).size());
            }
        }

        //generate missing statements -- list containing --missing text here stmts
        public static List<String> genStatement(LocalTime time,int count){
            List<String> list = new ArrayList<>();
            String str = PREFIX +" "+time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))+" "+ FILL_MSG;
            for(int i=0;i<count;i++){
                list.add(str);
            }
            return list;
        }

        public static int timeToInt(LocalTime time){
            return time.toSecondOfDay()/60 ;
        }

}
