package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    private static Hashtable<Integer,ArrayList<Integer>> dataset;

        public static void main(String[] args) {
            // write your code here
            dataset = new Hashtable<>();
            System.out.print("Specify data set file (*.csv): ");
            Scanner input = new Scanner(System.in);
            readDataSet(input.nextLine());
            CMeans cMeans = new CMeans(dataset);
            System.out.println("Applying Fussy C-Means");
            System.out.println("(Note: this might take few seconds depending on your processor's speed... )");
            try {
                cMeans.analyse();
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
            input.nextLine();
    }

    /**
     * Read the data set
     */
    private static void readDataSet(String fileName){
        try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String row;
            file.readLine();
            System.out.println("Reading data set ...");
            while((row = file.readLine()) != null){
                populateDataSet(new StringTokenizer(row,","));
            }
            System.out.println(dataset.size()>1?dataset.size()+" records found.":dataset.size()+" record found");
        } catch (FileNotFoundException e) {
            System.out.print(" "+e.getLocalizedMessage());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * This method groups the data set into its
     * original group.
     */
    private static void populateDataSet(StringTokenizer data){
        Iterator iterator = data.asIterator();
        ArrayList<Integer> record = new ArrayList<>();
        while(iterator.hasNext()){
            record.add(Integer.parseInt((String)iterator.next()));
        }
        dataset.put(record.get(0),record);
    }
}
