package com.company;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This is a GUIController class
 *
 * @author: Amowe Sunday Alexander
 * @version:
 * @date: 4/3/2019 @7:58 PM
 */
public class GUIController implements Initializable {
    //Class properties goes here
    private Hashtable<Integer,ArrayList<Integer>> dataset;
    private SimpleStringProperty message;

    @FXML
    private Label filename;

    @FXML
    private Label status;

    @FXML
    private JFXButton selectBtn;

    @FXML
    private JFXTextArea display;

    /**
     * Constructor
     */
    public GUIController() {
        //Constructor logic goes here
        dataset = new Hashtable<>();
        this.message = new SimpleStringProperty("Fuzzy C-Means Algorithm for Three (3) clusters\n");
    }

    @FXML
    void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select dataset file (*.csv)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.csv files only","*.csv"));

        //Get dataset
        File file = fileChooser.showOpenDialog(null);
        if(file != null){
            this.filename.setText(file.getName());
            try {
                Files.lines(Paths.get(file.getPath())).skip(1).forEach(
                        record -> setData(record)
                );
                this.status.setText("Status: "+this.dataset.size()+" Records found!");
                this.output("Status: "+this.dataset.size()+" Records found!");

                //compute C-means
                CMeans cMeans = new CMeans(dataset);
                cMeans.setStatus(this.status);
                cMeans.setOutputStream(this.message);
                this.output("Applying Fuzzy C-Means. Please wait (Note: this " +
                        "might take few seconds depending on your processor's speed... )");
                //Background Service
                Service service = new Service() {
                    @Override
                    protected Task createTask() {
                        return new Task() {
                            @Override
                            protected Object call() throws Exception {
                                cMeans.analyse();
                                output("\n\n");
                                output(cMeans.getReport().getName());
                                return null;
                            }
                        };
                    }
                };
                service.start();
                service.setOnSucceeded(e->{
                    try {
                        Files.lines(Paths.get(cMeans.getReport().getPath()))
                                .forEachOrdered(report->output(report));
                    } catch (IOException ex) {
                        this.status.setText(ex.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                this.status.setText("error: "+e.getLocalizedMessage());
            }

        }

    }

    /**
     * Read the data set
     */
    private void setData(String record){
        if(!record.isEmpty()){
            var data = new StringTokenizer(record,",");
            Iterator iterator = data.asIterator();
            ArrayList<Integer> row = new ArrayList<>();
            while(iterator.hasNext()){
                row.add(Integer.parseInt((String)iterator.next()));
            }
            dataset.put(row.get(0),row);
        }

    }

    /**
     * This method output report message to the
     * display GUI
     * @param message
     */
    private void output(String message){
        this.message.setValue(this.message.getValue()+message+"\n");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.display.textProperty().bind(this.message);
    }
}
