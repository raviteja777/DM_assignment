/**
 * Created by Raviteja on 15-04-2017.
 */
//weka libraries
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
//pojo
import java.util.ArrayList;
import java.util.Map;


/*  build a model using linear regression on training set
 *  there are two maps -- trainMap containing training set -- <timestamp,count> of all available message
 *  testset contains all timestamps whose counts are unknown
 */
public class  TimeStampAdjuster {

    //linear regression builds model
    LinearRegression reg = new LinearRegression();
    Map<Integer,Integer> trainMap ;
    int testdata ;

    //init attributes
     static Attribute timestamp = new Attribute("timestamp");
     static Attribute count = new Attribute("count");
     static ArrayList<Attribute> attributes = new ArrayList<>();
    //instances -- to build data set
     static Instances trainingSet ;

    static{
        //create a list of attributes
        attributes.add(timestamp);
        attributes.add(count);
        //add the list to a training set -- create empty table
        trainingSet = new Instances("rel",attributes,4);
        trainingSet.setClass(count);
    }


    public TimeStampAdjuster(Map<Integer,Integer> trainMap,int testdata){
        this.trainMap = trainMap;
        this.testdata = testdata;
    }

    //obtains missing count of lines  using linear regression
    protected int getMissingCount(){

        //clear all existing values in training set
        trainingSet.delete();

        System.out.println("for test data "+testdata);
        //lambda expression runs a loop and copies <key,val> from map
        trainMap.forEach((x,y)->{
            Instance inst = new DenseInstance(2);
            System.out.println("training set :" + x+" "+y);
            inst.setValue(timestamp,x);
            inst.setValue(count,y);
            trainingSet.add(inst);
        });


        double unknownCount = 0;
        //start regression
        try {
            reg.buildClassifier(trainingSet);
            //test the model on data whose count is missing
            Instance testInst = new DenseInstance(2);
            testInst.setValue(timestamp,testdata);
            unknownCount= reg.classifyInstance(testInst);

        } catch (Exception e) {
            System.out.println("----exception encountered during regression-----");
            e.printStackTrace();
        }
       // System.out.println("end");
        return (int)Math.ceil(unknownCount);
    }

    public int updateMissing(){
        int count = getMissingCount();
        if(count==0){
            count++;
        }
        return count;
    }



}
