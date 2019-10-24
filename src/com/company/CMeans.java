package com.company;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This is a CMeans class
 * @author: Amowe Sunday Alexander
 * @version:
 * @date: 2/9/2019 @1:49 PM
 */
public class CMeans {
    //Class properties goes here
    private Hashtable<Integer, ArrayList<Integer>> dataset;
    private SimpleStringProperty outputStream;
    private File report;
    private Label status;

    /**
     * Constructor
     */
    public CMeans(Hashtable<Integer, ArrayList<Integer>> dataset) {
        //Constructor logic goes here
        this.dataset = dataset;
    }

    /**
     * This method calculate the centroid for each
     * clusters by applying the centroid formula.
     * @return Hashtable<class, ids>
     */
    private Hashtable<Integer, ArrayList<Integer>> calculateCentroid(){
        Hashtable<Integer,ArrayList<Integer>> centroids = new Hashtable<>(); //this holds the centroid class and ids of each records.
        Iterator<ArrayList<Integer>> iterator = this.dataset.values().iterator();
        boolean same = true;
        //Search for records with same values
        while(iterator.hasNext()){
            var record = iterator.next();
            if(!centroids.containsKey(record.get(record.size()-1))){
                ArrayList<Integer> ids = new ArrayList<>();
                ids.add(record.get(0));
                centroids.put(record.get(record.size()-1), ids);
            }else{
                var centroid = dataset.get(centroids.get(record.get(record.size()-1)).get(0));
                for(int i = 1; i < record.size(); i++){
                    if(!record.get(i).equals(centroid.get(i))){
                        same = false;
                        break;
                    }
                }
                if(same){
                    centroids.get(record.get(record.size()-1)).add(record.get(0));
                }
            }
        }
        return centroids;
    }

    /**
     * This method set the dataset for analysis
     * @param dataset
     */
    public void setDataset(Hashtable<Integer, ArrayList<Integer>> dataset){
        this.dataset = dataset;
    }

    /**
     *This method clusters the data set.
     * @param centroid Hashtable<class, ids>
     * @return Hashtable<id, class>
     */
    public Hashtable<Integer,Integer> cluster(Hashtable<Integer, ArrayList<Integer>> centroid){
        Iterator<ArrayList<Integer>> iterator = this.dataset.values().iterator();
        Hashtable<Integer,Integer> cluster = new Hashtable<>();
        while(iterator.hasNext()){
            var record = iterator.next();
            var centroids = centroid.values().iterator();
            var centroidClasses = centroid.keys().asIterator();
            //Test if it is a member of a centriod
            if(centroid.get(record.get(record.size()-1)).contains(record.get(0))){
                cluster.put(record.get(0),record.get(record.size()-1));
            }else{
                Hashtable<Double, Integer> weights =  new Hashtable<>();
                while(centroids.hasNext() && centroidClasses.hasNext()){
                    var distance = 0;
                    var centClass = centroidClasses.next();
                    var centRecord = this.dataset.get(centroids.next().get(0));
                    for(int i = 0; i < centRecord.size() - 1; i++){
                        if(record.get(i).equals(centRecord.get(i))){
                            distance = distance + 1;
                        }
                    }
                    var totalDistance = centRecord.size()-1;
                    weights.put((double)distance/(double)totalDistance,centClass);
                }
                double highestSimilarity = (double) 0;
                Iterator<Double> similarities = weights.keys().asIterator();
                while(similarities.hasNext()){
                    var similarity = similarities.next();
                    if(highestSimilarity < similarity){
                        highestSimilarity = similarity;
                    }
                }
                cluster.put(record.get(0),weights.get(highestSimilarity));
            }
        }
        return cluster;
    }

    /**
     *
     * @param cluster Hashtable<id, class>
     */
    public double accuracy(Hashtable<Integer,Integer> cluster){
        ArrayList<Integer> accuracy = new ArrayList<>();
        Iterator<ArrayList<Integer>> iterator = this.dataset.values().iterator();
        while(iterator.hasNext()){
            var record = iterator.next();
            if(record.get(record.size() - 1).equals(cluster.get(record.get(0)))){
                accuracy.add(1);
            }
        }
        return (((double)accuracy.size()/(double) this.dataset.size()) * 100);
    }

    public void setStatus(Label status){
        this.status = status;
    }

    /**
     *
     */
    public void analyse() throws IOException {
        var start = System.currentTimeMillis();
        Platform.runLater(()->{
            this.status.setText("Status: Filtering data set...");
        });
        long time = 0;
        Platform.runLater(()->{
            this.status.setText("Status: Computing Centroids..");
        });
        this.append(" \n");
        Iterator<ArrayList<Integer>> iterator = this.calculateCentroid().values().iterator();
        var centroidFound = false;
        Platform.runLater(()->{
            this.status.setText("Status: Centroids computed successfully..");
        });
        while(iterator.hasNext()){
            if(iterator.next().size() > 1){
                centroidFound = true;
            }
        }
        this.show("Locating Centroid... ");
        if(centroidFound){
            this.show("Clustering data set ...");
           var cluster = this.cluster(this.calculateCentroid());
           this.show("Computing accuracy ...");
            this.show(String.format("Accuracy = %.2f",this.accuracy(cluster))+"%");
        }else{
            double highestAccuracy = 0.0;
            this.show("Clustering data set ...");
            var records = this.dataset.values().toArray();
            for(int i = 0; i < records.length; i++){
                this.show("Clustering data: "+(i+1));
                for (int j = i; j < records.length; j++) {
                    for (int k = j; k < records.length; k++) {
                        var record1 = (ArrayList)records[i];
                        var record2 = (ArrayList)records[j];
                        var record3 = (ArrayList)records[k];
                        Hashtable<Integer, ArrayList<Integer>> centroid  = new Hashtable<>();
                        //class 1
                        ArrayList<Integer> ids = new ArrayList<>();
                        ids.add((Integer) record1.get(0));
                        centroid.put(1,ids);

                        //class 2
                        ids = new ArrayList<>();
                        ids.add((Integer) record2.get(0));
                        centroid.put(2,ids);

                        //class 3
                        ids = new ArrayList<>();
                        ids.add((Integer) record3.get(0));
                        centroid.put(3,ids);
                        var cluster = this.cluster(centroid);
                        var accuracy = this.accuracy(cluster);
                        if(highestAccuracy < accuracy){
                            highestAccuracy = accuracy;
                            this.report = new File("report.txt");
                            if(!this.report.exists()){
                                this.report.createNewFile();
                            }
                            BufferedWriter writer = new BufferedWriter(new FileWriter(this.report));
                            ids = new ArrayList<>(cluster.keySet());
                            Collections.sort(ids);
                            var it = ids.iterator();
                            writer.write("\t\tReport");
                            writer.newLine();
                            writer.write("---------------------------------------------------------");
                            writer.newLine();
                            writer.write("total records: "+dataset.size()+" records found!");
                            writer.newLine();
                            writer.write("total clusters: 3 clusters found!");
                            time = System.currentTimeMillis() - start;
                            writer.newLine();
                            writer.write(String.format("Accuracy: %.2f", highestAccuracy)+"%");
                            writer.newLine();
                            writer.write("Time Taken: "+String.format("%.2f",(double)time/1000.0)+" seconds");
                            writer.newLine();
                            writer.newLine();
                            writer.write("Id\t|Cluster\t|Original\t|Accuracy");
                            writer.newLine();
                            writer.write("---------------------------------------------------------");
                            while(it.hasNext()){
                                writer.newLine();
                                var id = it.next();
                                writer.write(id+"\t|"+cluster.get(id)+"\t\t|");
                                writer.write(this.dataset.get(id).get(this.dataset.get(id).size() - 1)+"\t\t|");
                                if(this.dataset.get(id).get(this.dataset.get(id).size() - 1).equals(cluster.get(id))){
                                    writer.write("1");
                                }else {
                                    writer.write("0");
                                }

                            }
                            writer.close();
                        }
                    }
                }
                this.show("Clusters Computed successfully... \n");
            }

            this.append(String.format("Accuracy = %.2f", highestAccuracy)+"%");
            this.append("Time Taken: "+String.format("%.2f",(double)time/1000.0)+" seconds");
            this.append("The analysis statistics has been dumped into report.txt");
        }
    }

    /**
     * This method sets the output Stream
     * Channel.
     */
    protected void setOutputStream(SimpleStringProperty stringProperty){
        this.outputStream = stringProperty;
    }

    /**
     * This method appends a given message to the output
     * stream.
     * @param message
     */
    protected void append(String message){
        this.outputStream.setValue(this.outputStream.getValue()+message+"\n");
    }

    /**
     * This method returns the report file
     * @return
     */
    protected File getReport(){
        return  this.report;
    }

    /**
     * This method displays the status of the cmeans
     * @param message
     */
    protected void show(String message){
        Platform.runLater(()->{
            this.status.setText("Status: "+message);
        });
    }
}
